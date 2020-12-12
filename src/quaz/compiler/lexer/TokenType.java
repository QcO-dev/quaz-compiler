package quaz.compiler.lexer;

public enum TokenType {
	
	KEYWORD,
	IDENTIFIER,
	
	INT,
	DOUBLE,
	FLOAT,
	STRING,
	CHAR,
	BYTE,
	LONG,
	SHORT,
	
	LPAREN,
	RPAREN,
	LSQBR,
	RSQBR,
	EQUALS,
	COLON,
	SEMI,
	COMMA,
	ARROW,
	DOT,
	
	PLUS,
	MINUS,
	DIVIDE,
	MULTIPLY,
	MODULUS,
	
	AND,
	OR,
	
	BIT_AND,
	BIT_OR,
	BIT_XOR,
	
	BOOL_EQ,
	BOOL_NE,
	BOOL_NOT,
	BOOL_TRI_EQ,
	BOOL_TRI_NE,
	BOOL_GT,
	BOOL_GE,
	BOOL_LT,
	BOOL_LE,
	
	NEWLINE,
	INDENT,
	DEDENT,
	EOF
	
}
