package quaz.compiler.standardLibrary;

import java.util.ArrayList;

import quaz.compiler.compiler.values.Function;
import quaz.compiler.standardLibrary.config.Config;

public class StandardLibrary {

	public static ArrayList<Pair<?, ?>> STATIC_IMPORTS = new ArrayList<>();

	public static final Pair<?, ?>[] TYPE_REFERENCES = { new Pair<String, String>("String", "java/lang/String") };

	public static void buildStaticImports() {
		if(Config.getIniFile().get("BUILTINS", "println").equals("true")) {
			STATIC_IMPORTS.add(new Pair<String, Function>("Ljava/lang/Object;", new Function("println", "quaz/util/Stdout", "(Ljava/lang/Object;)V", false, null, "V")));
			STATIC_IMPORTS.add(new Pair<String, Function>("", new Function("println", "quaz/util/Stdout", "()V", false, null, "V")));
			STATIC_IMPORTS.add(new Pair<String, Function>("I", new Function("println", "quaz/util/Stdout", "(I)V", false, null, "V")));
			STATIC_IMPORTS.add(new Pair<String, Function>("D", new Function("println", "quaz/util/Stdout", "(D)V", false, null, "V")));
			STATIC_IMPORTS.add(new Pair<String, Function>("F", new Function("println", "quaz/util/Stdout", "(F)V", false, null, "V")));
			STATIC_IMPORTS.add(new Pair<String, Function>("Z", new Function("println", "quaz/util/Stdout", "(Z)V", false, null, "V")));
		}
		
		if(Config.getIniFile().get("BUILTINS", "input").equals("true")) {
			STATIC_IMPORTS.add(new Pair<String, Function>("Ljava/lang/Object;", new Function("input", "quaz/util/Stdin", "(Ljava/lang/Object;)Ljava/lang/String;", false, null, "Ljava/lang/String;")));
			STATIC_IMPORTS.add(new Pair<String, Function>("", new Function("input", "quaz/util/Stdin", "()Ljava/lang/String;", false, null, "Ljava/lang/String;")));
			STATIC_IMPORTS.add(new Pair<String, Function>("I", new Function("input", "quaz/util/Stdin", "(I)Ljava/lang/String;", false, null, "Ljava/lang/String;")));
			STATIC_IMPORTS.add(new Pair<String, Function>("D", new Function("input", "quaz/util/Stdin", "(D)Ljava/lang/String;", false, null, "Ljava/lang/String;")));
			STATIC_IMPORTS.add(new Pair<String, Function>("F", new Function("input", "quaz/util/Stdin", "(F)Ljava/lang/String;", false, null, "Ljava/lang/String;")));
			STATIC_IMPORTS.add(new Pair<String, Function>("Z", new Function("input", "quaz/util/Stdin", "(Z)Ljava/lang/String;", false, null, "Ljava/lang/String;")));
		}

	}

}
