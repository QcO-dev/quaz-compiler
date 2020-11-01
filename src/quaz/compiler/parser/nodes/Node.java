package quaz.compiler.parser.nodes;

import quaz.compiler.lexer.Token;
import quaz.compiler.position.Position;

public abstract class Node {
	
	Object value;
	Position start;
	Position end;
	
	public Node(Object value, Position start, Position end) {
		this.value = value;
		this.start = start;
		this.end = end;
	}
	
	public Node(Object value, Token token) {
		this.value = value;
		this.start = token.getStart();
		this.end = token.getEnd();
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Position getStart() {
		return start;
	}

	public void setStart(Position start) {
		this.start = start;
	}

	public Position getEnd() {
		return end;
	}

	public void setEnd(Position end) {
		this.end = end;
	}
	
}
