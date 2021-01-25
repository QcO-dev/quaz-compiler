package quaz.compiler.lexer;

import quaz.compiler.position.Position;

public class InlineEqualsToken extends Token {
	
	private final TokenType value;
	
	public InlineEqualsToken(TokenType value, Position start, Position end) {
		super(TokenType.INLINE_EQ, "", start, end);
		this.value = value;
	}

	public TokenType getInlineType() {
		return value;
	}
	
	@Override
	public String toString() {
		return "(INLINE_EQ : " + value + ")";
	}
	
}
