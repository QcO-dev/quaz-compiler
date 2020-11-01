package quaz.compiler.parser.nodes.block;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class WhileNode extends Node {

	private final Node condition;
	private final Node body;
	
	public WhileNode(Node condition, Node body, Position start, Position end) {
		super(condition, start, end);
		this.condition = condition;
		this.body = body;
	}

	public Node getCondition() {
		return condition;
	}

	public Node getBody() {
		return body;
	}
	
}
