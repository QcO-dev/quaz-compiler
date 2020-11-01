package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class MemberAccessNode extends Node {
	
	private final Node left;
	private final Node right;
	
	public MemberAccessNode(Node left, Node right, Position start, Position end) {
		super(left, start, end);
		this.left = left;
		this.right = right;
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}
	
	@Override
	public String toString() {
		return left + "." + right;
	}
	
}
