package quaz.compiler.parser.nodes.function;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class ArgumentListNode extends Node {

	final Node[] value;
	
	public ArgumentListNode(Node[] value, Position start, Position end) {
		super(value, start, end);
		this.value = value;
	}
	
	@Override
	public String toString() {
		
		String val = "";
		
		for(int i = 0; i < value.length; i++) {
			val += value[i] + (i == value.length-1 ? "" : ", ");
		}
		
		return "[" + val + "]";
	}

	public Node[] getValue() {
		return value;
	}
	
}
