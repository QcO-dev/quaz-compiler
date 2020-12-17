package quaz.compiler.parser.nodes.operation;

import quaz.compiler.lexer.TokenType;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class UnaryOperationNode extends Node {

	private final TokenType type;
	private final Node right;
	
	public UnaryOperationNode(TokenType type, Node value, Position start, Position end) {
		super(value, start, end);
		this.type = type;
		this.right = value;
	}

	public TokenType getType() {
		return type;
	}

	public Node getRight() {
		return right;
	}

}
