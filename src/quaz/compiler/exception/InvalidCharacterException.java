package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class InvalidCharacterException extends QuazException {

	public InvalidCharacterException(String message, Position pos) {
		super(message, pos, 0xFF810000 | 'C');
	}

	private static final long serialVersionUID = -3156139267858278375L;

	
	
}
