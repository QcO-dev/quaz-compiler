package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class InvalidEscapeException extends QuazException {
	public InvalidEscapeException(String message, Position pos) {
		super(message, pos, 0xFF810000 | 'e');
	}

	private static final long serialVersionUID = 4877344917470502754L;
	
}
