package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class StringNode extends Node {
	
	String value;
	
	public StringNode(String value, Token token) {
		super(value, token);
		this.value = value;
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}
	
}
