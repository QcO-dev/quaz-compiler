package quaz.compiler.parser.nodes.function;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class FunctionDefinitionNode extends Node {
	
	private Node arguments;
	private Node statement;
	private Token id;
	private Token returnType;
	
	public FunctionDefinitionNode(Token id, Node arguments, Token returnType, Node statement, Position start, Position end) {
		super(id, start, end);
		this.id = id;
		this.arguments = arguments;
		this.statement = statement;
		this.returnType = returnType;
	}

	public Node getArguments() {
		return arguments;
	}

	public void setArguments(Node arguments) {
		this.arguments = arguments;
	}

	public Node getStatement() {
		return statement;
	}

	public void setStatement(Node statement) {
		this.statement = statement;
	}

	public Token getId() {
		return id;
	}

	public void setId(Token id) {
		this.id = id;
	}
	
	public Token getReturnType() {
		return returnType;
	}

	@Override
	public String toString() {
		return "{function " + id.getValue() + " " + arguments + " " + statement + "}"; 
	}
	
}
