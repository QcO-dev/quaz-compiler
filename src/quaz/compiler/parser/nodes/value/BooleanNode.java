package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class BooleanNode extends Node {
	
	private final boolean val;
	
	public BooleanNode(boolean value, Token token) {
		super(value, token);
		this.val = value;
	}

	public boolean getVal() {
		return val;
	}
	
	@Override
	public String toString() {
		return "" + val;
	}
	
}
