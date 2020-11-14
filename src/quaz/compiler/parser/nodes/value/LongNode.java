package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class LongNode extends Node {

	long val;
	
	public LongNode(String value, Token token) {
		super(value, token);
		this.val = Long.parseLong(value);
	}

	@Override
	public String toString() {
		return val + "l";
	}
	
	public long getVal() {
		return val;
	}
	
}
