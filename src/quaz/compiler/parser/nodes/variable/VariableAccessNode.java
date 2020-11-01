package quaz.compiler.parser.nodes.variable;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class VariableAccessNode extends Node {

	private String name;
	
	public VariableAccessNode(String value, Token token) {
		super(value, token);
		this.name = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
