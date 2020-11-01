package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class InstanceNode extends Node {

	private final String type;
	private final Node arguments;
	
	public InstanceNode(String type, Node arguments, Position start, Position end) {
		super(type, start, end);
		this.type = type;
		this.arguments = arguments;
	}

	public String getType() {
		return type;
	}

	public Node getArguments() {
		return arguments;
	}
	
	@Override
	public String toString() {
		return "new " + type + arguments;
	}
	
}
