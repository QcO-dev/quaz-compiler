package quaz.compiler.lexer;

import java.io.File;
import java.util.ArrayList;

import quaz.compiler.exception.IndentionLevelException;
import quaz.compiler.exception.InvalidCharacterException;
import quaz.compiler.exception.InvalidEscapeException;
import quaz.compiler.exception.InvalidNumberException;
import quaz.compiler.position.Position;

public class Lexer {
	
	private int index = 0;
	private String currentChar;
	private String text;
	private int indent = 0;
	private int indentLevel = 0;
	private int line = 1;
	private int column = 0;
	private File file;
	
	public Token[] lex(File file, String text) throws InvalidCharacterException, IndentionLevelException, InvalidEscapeException, InvalidNumberException {
		this.text = text;
		this.file = file;
		
		advance();
		
		ArrayList<Token> tokens = new ArrayList<>();
		
		while(currentChar != null) {
			
			if(currentChar.matches("[ \t\r]")) {
				advance();
				continue;
			}
			
			else if(currentChar.matches("[a-zA-Z_]")) {
				tokens.add(identifier());
			}
			
			else if(currentChar.equals("\n")) {
				newline(tokens);
				continue;
			}
			
			else if(currentChar.equals("/")) {
				advance();
				
				if(currentChar != null && currentChar.equals("*")) {
					while(currentChar != null) {
						if(currentChar.equals("*")) {
							advance();
							if(currentChar != null && currentChar.equals("/")) {
								advance();
								break;
							}
						}
						advance();
						
					}
				}
				
				else if(currentChar != null && currentChar.equals("/")) {
					while(currentChar != null && !currentChar.equals("\n")) {
						advance();
					}
				}
				
				else {
					reverse();
					tokens.add(new Token(TokenType.DIVIDE, new Position(column, line, file)));
				}
				
			}
			
			else if(currentChar.equals(".")) {
				tokens.add(new Token(TokenType.DOT, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("(")) {
				tokens.add(new Token(TokenType.LPAREN, new Position(column, line, file)));
			}
			
			else if(currentChar.equals(")")) {
				tokens.add(new Token(TokenType.RPAREN, new Position(column, line, file)));
			}
			
			else if(currentChar.equals(",")) {
				tokens.add(new Token(TokenType.COMMA, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("=")) {
				tokens.add(equals());
			}
			
			else if(currentChar.equals("!")) {
				tokens.add(not());
			}
			
			else if(currentChar.equals("<")) {
				tokens.add(lessThan());
			}
			
			else if(currentChar.equals(">")) {
				tokens.add(greaterThan());
			}
			
			else if(currentChar.equals("-")) {
				tokens.add(minus());
			}
			
			else if(currentChar.equals(";")) {
				tokens.add(new Token(TokenType.SEMI, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("+")) {
				tokens.add(new Token(TokenType.PLUS, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("*")) {
				tokens.add(new Token(TokenType.MULTIPLY, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("&")) {
				tokens.add(and());
			}
			
			else if(currentChar.equals("|")) {
				tokens.add(or());
			}
			
			else if(currentChar.equals("^")) {
				tokens.add(new Token(TokenType.BIT_XOR, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("%")) {
				tokens.add(new Token(TokenType.MODULUS, new Position(column, line, file)));
			}
			
			else if(currentChar.equals(":")) {
				tokens.add(new Token(TokenType.COLON, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("[")) {
				tokens.add(new Token(TokenType.LSQBR, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("]")) {
				tokens.add(new Token(TokenType.RSQBR, new Position(column, line, file)));
			}
			
			else if(currentChar.equals("\"")) {
				tokens.add(string());
			}
			
			else if(currentChar.matches("[0-9]")) {
				tokens.add(number());
			}
			
			else if(currentChar.equals("'")) {
				tokens.add(character());
			}
			
			else {
				throw new InvalidCharacterException("Unexpected Character: \'" + currentChar + "\'", new Position(column, line, file));
			}
			
			advance();
		}
		
		if(indentLevel != 0) {
			
			for(int i = 0; i < indentLevel; i++) {
				tokens.add(new Token(TokenType.DEDENT, new Position(column, line, file)));
			}
			
		}
		
		return tokens.toArray(new Token[] {});
		
	}
	
	private void advance() {
		currentChar = index < text.length() ? "" + text.charAt(index) : null;
		
		if(currentChar != null && currentChar.equals("\t"))
			column += 4;
		else
			column++;
		
		if(currentChar != null && currentChar.equals("\n")) {
			line++;
			column = 0;
		}
		
		index++;
	}
	
	private void reverse() {
		index--;
		
		currentChar = index < text.length() ? "" + text.charAt(index) : null;
		
		if(currentChar != null && currentChar.equals("\t"))
			column -= 4;
		else
			column--;
		
	}
	
	private Token minus() throws InvalidNumberException {
		
		Position start = new Position(column, line, file);
		
		Position end = new Position(column, line, file);
		
		advance();
		
		TokenType type = TokenType.MINUS;
		
		if(currentChar != null && currentChar.equals(">")) {
			type = TokenType.ARROW;
			end = new Position(column, line, file);
		}
		
		else if(currentChar != null && currentChar.matches("[0-9]")) {
			Token number = number();
			
			number.setStart(start);
			
			number.setValue("-" + number.getValue());
			
			return number;
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
	}
	
	private Token and() {
		
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.BIT_AND;
		
		advance();
		
		if(currentChar != null && currentChar.equals("&")) {
			type = TokenType.AND;
			end = new Position(column, line, file);
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}
	
	private Token or() {
		
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.BIT_OR;
		
		advance();
		
		if(currentChar != null && currentChar.equals("|")) {
			type = TokenType.OR;
			end = new Position(column, line, file);
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}
	
	private Token equals() {
		
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.EQUALS;
		
		advance();
		
		if(currentChar != null && currentChar.equals("=")) {
			type = TokenType.BOOL_EQ;
			end = new Position(column, line, file);
			
			advance();
			
			if(currentChar != null && currentChar.equals("=")) {
				type = TokenType.BOOL_TRI_EQ;
				end = new Position(column, line, file);
			}
			else {
				reverse();
			}
			
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}
	
	private Token not() {
		
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.BOOL_NOT;
		
		advance();
		
		if(currentChar != null && currentChar.equals("=")) {
			type = TokenType.BOOL_NE;
			end = new Position(column, line, file);
			
			advance();
			
			if(currentChar != null && currentChar.equals("=")) {
				type = TokenType.BOOL_TRI_NE;
				end = new Position(column, line, file);
			}
			else {
				reverse();
			}
			
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}
	
	private Token lessThan() {
		
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.BOOL_LT;
		
		advance();
		
		if(currentChar != null && currentChar.equals("=")) {
			type = TokenType.BOOL_LE;
			end = new Position(column, line, file);
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}

	private Token greaterThan() {
	
		Position start = new Position(column, line, file);
		Position end = new Position(column, line, file);
		
		TokenType type = TokenType.BOOL_GT;
		
		advance();
		
		if(currentChar != null && currentChar.equals("=")) {
			type = TokenType.BOOL_GE;
			end = new Position(column, line, file);
		}
		
		else {
			reverse();
		}
		
		return new Token(type, "", start, end);
		
	}
	
	private void newline(ArrayList<Token> tokens) throws IndentionLevelException {
		
		tokens.add(new Token(TokenType.NEWLINE, new Position(column, line, file)));
		
		advance();
		
		// If next line starts with an indent
		if(currentChar != null && currentChar.matches("[\t ]")) {
			
			Position start = new Position(column, line, file);
			
			// If the indent size was unset, set the indent size for the file and add an indent
			if(indent == 0) {
				while(currentChar != null && currentChar.matches("[\t ]")) {
					
					indent += currentChar.matches("[\t]") ? 4 : 1;
					advance();
					
				}
				tokens.add(new Token(TokenType.INDENT, new Position(column, line, file)));
				indentLevel++;
			}
			
			else {
				
				int indentSpace = 0;
				
				
				// Counts the amount of whitespace based on indent
				while(currentChar != null && currentChar.matches("[\t ]")) {
					
					indentSpace += currentChar.matches("[\t]") ? 4 : 1;
					
					advance();
					
				}
				
				double dIndentSpace = (double) indentSpace; // How big this indent is
				double dIndent = (double) indent; // How big an indent is
				
				if(dIndentSpace / dIndent != Math.floor(dIndentSpace / dIndent)) {
					throw new IndentionLevelException("Indention level is invalid, indention level for this file is " + indent + " spaces.", start, new Position(column, line, file));
				}
				
				int indents = indentSpace / indent;
				
				if(indents > indentLevel) {
					tokens.add(new Token(TokenType.INDENT, new Position(column, line, file)));
					indentLevel = indents;
				}
				else if(indents < indentLevel) {
					tokens.add(new Token(TokenType.DEDENT, new Position(column, line, file)));
					tokens.add(new Token(TokenType.NEWLINE, new Position(column, line, file)));
					indentLevel = indents;
				}
				
			}
			
			// Outputs the correct amount of whitespace.
			//for(int i = 0; i < indentLevel; i++) {
			//	tokens.add(new Token(TokenType.INDENT));
			//}
			
		}
		else {
			
			//System.out.println('"' + currentChar + '"');
			
			if(currentChar != null && currentChar.equals("\n")) {
				return;
			}
			
			if(indentLevel != 0) {
				for(int i = 0; i < indentLevel; i++) {
					tokens.add(new Token(TokenType.DEDENT, new Position(column, line, file)));
				}
				tokens.add(new Token(TokenType.NEWLINE, new Position(column, line, file)));
				indentLevel = 0;
			}
		}
		
	}
	
	private Token identifier() {
		
		Position start = new Position(column, line, file);
		
		String word = currentChar;
		
		advance();
		
		while(currentChar != null && currentChar.matches("[a-zA-Z0-9_]")) {
			word += currentChar;
			
			advance();
		}
		reverse();
		
		TokenType type = TokenType.IDENTIFIER;
		
		if(Token.KEYWORDS_ARRAY.contains(word)) {
			type = TokenType.KEYWORD;
		}
		
		return new Token(type, word, start, new Position(column, line, file));
		
	}
	
	private Token number() throws InvalidNumberException {
		
		Position start = new Position(column, line, file);
		
		String word = currentChar;
		
		TokenType type = TokenType.INT;
		
		advance();
		
		//TODO Hexadecimal and Binary support goes here.
		
		int decimalPointCount = 0;
		
		while(currentChar != null && currentChar.matches("[0-9\\.]")) {
			word += currentChar;
			
			if(currentChar.equals(".")) {
				decimalPointCount++;
			}
			
			advance();
		}
		reverse();
		
		if(decimalPointCount == 1) {
			type = TokenType.DOUBLE;
		}
		else if(decimalPointCount > 1) {
			throw new InvalidNumberException("Numbers may only contain a single decimal place.", start, new Position(column, line, file));
		}
		
		if(currentChar != null && currentChar.toLowerCase().equals("d")) {
			type = TokenType.DOUBLE;
			advance();
		}
		else if(currentChar != null && currentChar.toLowerCase().equals("f")) {
			type = TokenType.FLOAT;
			advance();
		}
		
		else if(currentChar != null && currentChar.toLowerCase().equals("b")) {
			type = TokenType.BYTE;
			advance();
		}
		
		else if(currentChar != null && currentChar.toLowerCase().equals("l")) {
			type = TokenType.LONG;
			advance();
		}
		
		else if(currentChar != null && currentChar.toLowerCase().equals("s")) {
			type = TokenType.SHORT;
			advance();
		}
		
		tryValidNumber(type, word, start, new Position(column, line, file));
		
		return new Token(type, word, start, new Position(column, line, file));
		
	}
	
	private void tryValidNumber(TokenType type, String word, Position start, Position end) throws InvalidNumberException {
		
		switch(type) {
			case INT: {
				try {
					Integer.parseInt(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid integer: " + word, start, end);
				}
				break;
			}
			
			case DOUBLE: {
				try {
					Double.parseDouble(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid double: " + word, start, end);
				}
				break;
			}
			
			case FLOAT: {
				try {
					Float.parseFloat(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid float: " + word, start, end);
				}
				break;
			}
			
			case BYTE: {
				try {
					Byte.parseByte(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid byte: " + word, start, end);
				}
				break;
			}
			
			case LONG: {
				try {
					Long.parseLong(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid long: " + word, start, end);
				}
				break;
			}
			
			case SHORT: {
				try {
					Short.parseShort(word);
				} catch(NumberFormatException e) {
					throw new InvalidNumberException("Invalid short: " + word, start, end);
				}
				break;
			}
			
			default:
				break;
		}
		
	}

	private Token string() throws InvalidEscapeException {
		
		Position start = new Position(column, line, file);
		
		String value = "";
		
		advance();
		
		boolean escape = false;
		
		while(currentChar != null && ( escape || !currentChar.equals("\""))) {
			
			if(currentChar.equals("\\")) {
				escape = true;
				advance();
				continue;
			}
			
			if(escape) {
				
				escape = false;
				
				switch(currentChar) {
					case "\\":
						value += "\\";
						break;
					case "b":
						value += "\b";
						break;
					case "n":
						value += "\n";
						break;
					case "t":
						value += "\t";
						break;
					case "r":
						value += "\r";
						break;
					case "f":
						value += "\f";
						break;
					case "\"":
						value += "\"";
						break;
					default:
						throw new InvalidEscapeException("Unknown Escape Character: \'\\" + currentChar + "\'.", new Position(column, line, file));
				}
				advance();
				continue;
			}
			
			value += currentChar;
			
			advance();
			
		}
		
		return new Token(TokenType.STRING, value, start, new Position(column, line, file));
		
	}
	
	private Token character() throws InvalidEscapeException, InvalidCharacterException {
		Position start = new Position(column, line, file);
		
		String value = "";
		
		advance();
		
		boolean escape = false;
		
		int length = 0;
		
		while(currentChar != null && ( escape || !currentChar.equals("'"))) {
			
			if(length > 1) {
				throw new InvalidCharacterException("Character must only be one character.", new Position(column, line, file));
			}
			
			if(currentChar.equals("\\")) {
				escape = true;
				advance();
				continue;
			}
			
			if(escape) {
				
				escape = false;
				
				switch(currentChar) {
					case "\\":
						value += "\\";
						break;
					case "b":
						value += "\b";
						break;
					case "n":
						value += "\n";
						break;
					case "t":
						value += "\t";
						break;
					case "r":
						value += "\r";
						break;
					case "f":
						value += "\f";
						break;
					case "\"":
						value += "\"";
						break;
					default:
						throw new InvalidEscapeException("Unknown Escape Character: \'\\" + currentChar + "\'.", new Position(column, line, file));
				}
				advance();
				length++;
				continue;
			}
			
			value += currentChar;
			
			length++;
			
			advance();
			
		}
		
		if(length > 1) {
			throw new InvalidCharacterException("Character must only be one character.", new Position(column, line, file));
		}
		
		if(length == 0) {
			throw new InvalidCharacterException("Character must be at least one character.", new Position(column, line, file));
		}

		return new Token(TokenType.CHAR, value, start, new Position(column, line, file));
	}
	
}
