package quaz.compiler.exception;

import quaz.compiler.position.Position;

public class QuazException extends Exception {
	private static final long serialVersionUID = -2245033305709622232L;
	
	private Position start;
	private Position end;
	private String message;
	
	public QuazException(String message, Position pos) {
		this.message = message;
		start = pos;
		end = pos;
	}
	
	public QuazException(String message, Position start, Position end) {
		this.message = message;
		this.start = start;
		this.end = end;
	}
	
	@Override
	// Returns a String in the form EXCEPTION_NAME in FILE_NAME at line LINE(S), column COLUMN(S): MESSAGE
	public String toString() {
		
		boolean isSameChar = start.getColumn() == end.getColumn();
		
		boolean isSameLine = start.getLine() == end.getLine();
		
		return this.getClass().getSimpleName()
				+ " in " + start.getFile().getName()
				+ " at line "
				+ (isSameLine ? start.getLine() : start.getLine() + "-" + end.getLine())
				+ ", column "
				+ (isSameChar ? start.getColumn() : start.getColumn() + "-" + end.getColumn())
				+ ": \n"
				+ message;
		
	}
	
}
