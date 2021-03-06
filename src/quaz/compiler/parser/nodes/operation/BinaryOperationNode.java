package quaz.compiler.parser.nodes.operation;

import quaz.compiler.lexer.TokenType;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class BinaryOperationNode extends Node {
	
	private final TokenType type;
	private final Node left;
	private final Node right;
	private boolean isInlineOp = false;
	
	public BinaryOperationNode(TokenType type, Node left, Node right, Position start, Position end) {
		super(type, start, end);
		this.type = type;
		this.left = left;
		this.right = right;
	}
	
	public BinaryOperationNode(TokenType type, Node left, Node right, Position start, Position end, boolean isInlineOp) {
		super(type, start, end);
		this.type = type;
		this.left = left;
		this.right = right;
		this.isInlineOp = isInlineOp;
	}
	
	@Override
	public String toString() {
		return "{" + left + " " + type + " " + right + "}";
	}

	public TokenType getType() {
		return type;
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}

	public boolean isInlineOp() {
		return isInlineOp;
	}
}
