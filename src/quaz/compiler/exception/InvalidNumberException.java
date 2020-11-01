package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class InvalidNumberException extends QuazException {

	private static final long serialVersionUID = -1070597130480992206L;

	public InvalidNumberException(String message, Position start, Position end) {
		super(message, start, end);
	}
	
	
	
}
