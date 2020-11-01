package quaz.compiler.parser.nodes.function;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class FunctionCallNode extends Node {

	Token id;
	Node arguments;
	
	public FunctionCallNode(Token id, Node arguments, Position start, Position end) {
		super(id, start, end);
		this.id = id;
		this.arguments = arguments;
	}

	public Token getId() {
		return id;
	}

	public void setId(Token id) {
		this.id = id;
	}

	public Node getArguments() {
		return arguments;
	}

	public void setArguments(Node arguments) {
		this.arguments = arguments;
	}
	
	@Override
	public String toString() {
		return "{call " + id.getValue() + " " + arguments + "}";
	}
	
}
