package quaz.compiler.parser.nodes.function;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class ReturnNode extends Node {

	public ReturnNode(Object value, Position start, Position end) {
		super(value, start, end);
	}
	
	@Override
	public String toString() {
		return "return " + getValue();
	}
	
}
