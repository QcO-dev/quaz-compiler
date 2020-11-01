package quaz.compiler.parser.nodes.block;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class ForNode extends Node {
	
	private final Node init;
	private final Node condition;
	private final Node increment;
	private final Node body;
	
	public ForNode(Node init, Node condition, Node increment, Node body, Position start, Position end) {
		super(init, start, end);
		this.init = init;
		this.condition = condition;
		this.increment = increment;
		this.body = body;
	}
	
	@Override
	public String toString() {
		return "for(" + init + "; " + condition + "; " + increment + ") {" + body + "}";
	}
	
	
	public Node getInit() {
		return init;
	}
	public Node getCondition() {
		return condition;
	}
	public Node getIncrement() {
		return increment;
	}

	public Node getBody() {
		return body;
	}
	
}
