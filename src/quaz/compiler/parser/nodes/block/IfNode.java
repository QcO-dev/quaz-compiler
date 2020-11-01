package quaz.compiler.parser.nodes.block;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class IfNode extends Node {

	private final Node condition;
	private final Node body;
	private final Node elseBody;
	
	public IfNode(Node condition, Node body, Node elseBody, Position start, Position end) {
		super(condition, start, end);
		this.condition = condition;
		this.body = body;
		this.elseBody = elseBody;
	}

	public Node getCondition() {
		return condition;
	}

	public Node getBody() {
		return body;
	}

	public Node getElseBody() {
		return elseBody;
	}
	
	@Override
	public String toString() {
		return "if(" + condition + ") {" + body + "} " + elseBody != null ? "else" + "{" + elseBody + "}" : "";
	}
	
}
