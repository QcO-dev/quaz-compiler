package quaz.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.datumware.cli.Cli;
import com.datumware.cli.Default;
import com.datumware.cli.Flag;
import com.datumware.cli.MainMethod;
import com.datumware.cli.Option;

import quaz.compiler.compiler.Compiler;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.exception.IndentionLevelException;
import quaz.compiler.exception.InvalidCharacterException;
import quaz.compiler.exception.InvalidEscapeException;
import quaz.compiler.exception.InvalidNumberException;
import quaz.compiler.exception.UnexpectedTokenException;
import quaz.compiler.lexer.Lexer;
import quaz.compiler.lexer.Token;
import quaz.compiler.parser.Parser;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.standardLibrary.Pair;
import quaz.compiler.standardLibrary.StandardLibrary;
import quaz.compiler.standardLibrary.config.Config;
import quaz.util.IniFile;

@Cli
public class CliHandler {

	// TODO Add support for operations in compiler

	@Flag(name = "Async", 
			description = "Sets if the compiler should use asynchronous compilation for supplied files", 
			shortDescription = "Compiles asynchronously", 
			trueNames = {"-a", "--async" }, 
			falseNames = { "-s", "--sync" }, 
			defaultValue = false
			)
	public boolean async;

	@Flag(name = "Lexer Output",
			description = "Sets if the compiler should output the lexers output and not the compiled class (Only allows for one file to be compiled)", 
			shortDescription = "Enable Lexer Output", trueNames = {"-l", "--lexer" }, 
			falseNames = { "--no-lexer" }, 
			defaultValue = false
			)
	public boolean lex;

	@Flag(name = "Parser Output", 
			description = "Sets if the compiler should output the parsers output and not the compiled class (Only allows for one file to be compiled)", 
			shortDescription = "Enable Parser Output", 
			trueNames = {"-p", "--parser" }, 
			falseNames = { "--no-parser" }, 
			defaultValue = false
			)
	public boolean parse;

	@Option(name = "Configuration File", 
			description = "Sets the configuration File to use with the compiler", 
			shortDescription = "Sets the config file", 
			names = {"-c", "--config" }, 
			defaultValue = ""
			)
	public String configFile;

	@Default
	public String[] args;

	private final static int ERROR_CODE = 0x20510000;

	@MainMethod
	public void main() {

		if(args.length == 0) {
			System.err.println("Missing file argument");
			System.exit(ERROR_CODE);
		}
		
		Config.buildDefaults();
		if(!configFile.isEmpty()) {

			try {
				IniFile ini = IniFile.parseINI(new FileReader(configFile));
				ini.setDefaults(Config.getDefaults());
				Config.setIniFile(ini);
			} catch(IOException e) {
				System.err.println("Could not open file: " + configFile);
				System.exit(ERROR_CODE + 10);
			}

		}
		else {
			Config.setIniFile(new IniFile(Config.getDefaults()));
		}
		
		StandardLibrary.buildStaticImports();

		Token.buildArrays();

		// TODO Make system.exit calls return values as to not break the compilation
		// (maybe)
		if(!async) {
			for(String filename : args) {
				compile(filename);
			}
		} else {
			Arrays.asList(args).parallelStream().forEach(this::compile);
		}

	}

	private void compile(String filename) {

		try {

			File f = new File(filename);

			if(!f.exists()) {
				System.err.println("File does not exist: " + filename);
				System.exit(ERROR_CODE + 1);
			}

			if(f.isDirectory()) {
				System.err.println("File is a directory: " + filename);
				System.exit(ERROR_CODE + 2);
			}

			// Lex, parse and compile source code.

			// String text = new String(Files.readAllBytes(f.toPath()));

			String text = "";

			String line;

			try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
				while((line = reader.readLine()) != null) {
					text += line + "\n";
				}
			}

			Lexer lexer = new Lexer();

			Token[] tokens = lexer.lex(f, text);

			if(lex) {
				Arrays.asList(tokens).forEach(System.out::println);

				System.exit(0);
			}

			Parser parser = new Parser();

			Node ast = parser.parse(tokens);

			if(parse) {
				System.out.println(ast);

				System.exit(0);
			}

			Compiler compiler = new Compiler();

			// byte[] bytes = compiler.compile(ast);

			Pair<String, byte[]> compilerResult = compiler.compile(ast);

			byte[] bytes = compilerResult.getSecond();

			String packageName = compilerResult.getFirst();

			File outputFile = new File(f.getParent() + "/" + packageName.replace(".", "/") + ".class");

			outputFile.getParentFile().mkdirs();

			outputFile.createNewFile();

			try(FileOutputStream stream = new FileOutputStream(outputFile)) {
				stream.write(bytes);
			}

		} catch(FileNotFoundException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 1);
		} catch(IOException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 3);
		} catch(InvalidCharacterException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 4);
		} catch(IndentionLevelException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 5);
		} catch(InvalidEscapeException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 6);
		} catch(InvalidNumberException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 7);
		} catch(UnexpectedTokenException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 8);
		} catch(CompilerLogicException e) {
			System.err.println(e.toString());
			System.exit(ERROR_CODE + 9);
		}

	}

}
