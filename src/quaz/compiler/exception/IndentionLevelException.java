package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class IndentionLevelException extends QuazException {
	
	public IndentionLevelException(String message, Position start, Position end) {
		super(message, start, end, 0xFF810000 | 'i');
	}

	private static final long serialVersionUID = 2251997883044251122L;
	
	
	
}
