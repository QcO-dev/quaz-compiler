package quaz.compiler.parser.nodes.operation;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class ArrayIndexNode extends Node {
	
	private final Node index;
	private final Node left;
	
	public ArrayIndexNode(Node left, Node index, Position start, Position end) {
		super(left, start, end);
		this.index = index;
		this.left = left;
	}

	public Node getIndex() {
		return index;
	}

	public Node getLeft() {
		return left;
	}
	
	@Override
	public String toString() {
		return left + "[" + index + "]";
	}
	
}
