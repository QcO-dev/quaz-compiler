package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class CompilerLogicException extends QuazException {

	private static final long serialVersionUID = -1904195409614139286L;

	public CompilerLogicException(String message, Position start, Position end) {
		super(message, start, end);
	}
	
	
	
}
