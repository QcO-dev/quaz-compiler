package quaz.compiler.exception;

import quaz.compiler.lexer.Token;
import quaz.compiler.position.Position;

public class UnexpectedTokenException extends QuazException {
	private static final long serialVersionUID = -6675174848291232070L;
	
	public UnexpectedTokenException(String message, Token token) {
		
		super(message, token.getStart(), token.getEnd());
		
	}
	
	public UnexpectedTokenException(String message, Position start, Position end) {
		super(message, start, end);
	}
	
	
}
