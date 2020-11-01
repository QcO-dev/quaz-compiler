package quaz.compiler.lexer;

import java.util.ArrayList;

import quaz.compiler.position.Position;

public class Token {
	
	private static final String[] KEYWORDS = {
		"var",
		"function",
		"void",
		"return",
		"import",
		"package",
		"new",
		"as",
		"int",
		"double",
		"float",
		"true",
		"false",
		"boolean",
		"if",
		"else",
		"while",
		"for",
		"pass",
		"break",
		"continue"
	};
	
	// These define what are keywords in Quaz that are not keywords in Java.
	private static final String[] JAVA_NON_KEYWORDS = {
		"var",
		"function",
		"as",
		"pass"
	};
	
	private static final String[] TYPE_KEYWORDS = {
		"int",
		"double",
		"float",
		"boolean"
	};
	
	public static final ArrayList<String> KEYWORDS_ARRAY = new ArrayList<>();
	
	public static final ArrayList<String> TYPE_KEYWORDS_ARRAY = new ArrayList<>();
	
	public static final ArrayList<String> JAVA_NON_KEYWORDS_ARRAY = new ArrayList<>();
	
	public static void buildArrays() {
		
		for(String s : KEYWORDS) {
			KEYWORDS_ARRAY.add(s);
		}
		
		for(String s : TYPE_KEYWORDS) {
			TYPE_KEYWORDS_ARRAY.add(s);
		}
		
		for(String s : JAVA_NON_KEYWORDS) {
			JAVA_NON_KEYWORDS_ARRAY.add(s);
		}
	}
	
	private String value;
	private TokenType type;
	private Position start;
	private Position end;
	
	
	
	public Token(TokenType type, String value, Position start, Position end) {
		this.value = value;
		this.type = type;
		this.start = start;
		this.end = end;
	}
	
	public Token(TokenType type, Position pos) {
		this.value = "";
		this.type = type;
		this.start = pos;
		this.end = pos;
	}
	
	@Override
	public String toString() {
		return value.equals("") ? "(" + type + ")" : "(" + type + ": " + value + ")";
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}

	public Position getStart() {
		return start;
	}

	public void setStart(Position start) {
		this.start = start;
	}

	public Position getEnd() {
		return end;
	}

	public void setEnd(Position end) {
		this.end = end;
	}
	
	public boolean matches(TokenType type) {
		return this.type == type;
	}
	
	public boolean matches(TokenType type, String value) {
		return this.type == type && this.value.equals(value);
	}
	
}
