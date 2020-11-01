package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class IntNode extends Node {

	int val;
	
	public IntNode(String value, Token token) {
		super(value, token);
		
		this.val = Integer.parseInt(value);
		
	}

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}
	
	@Override
	public String toString() {
		return "" + val;
	}
	
}
