package quaz.compiler.lexer;

import quaz.compiler.position.Position;

public class ExprStringToken extends Token {
	
	private final Token[][] exprs;
	
	public ExprStringToken(Token[][] exprs, String value, Position start, Position end) {
		super(TokenType.EXPR_STRING, value, start, end);
		this.exprs = exprs;
	}

	public Token[][] getExprs() {
		return exprs;
	}
	
}
