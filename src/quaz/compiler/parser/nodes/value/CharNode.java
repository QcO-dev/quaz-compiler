package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class CharNode extends Node {
	
	private final char val;
	
	public CharNode(String v, Token token) {
		super(v.charAt(0), token);
		this.val = v.charAt(0);
	}
	
	@Override
	public String toString() {
		return "'" + val + "'";
	}
	
	public char getVal() {
		return val;
	}

}
