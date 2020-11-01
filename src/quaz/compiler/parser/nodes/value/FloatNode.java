package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class FloatNode extends Node {
	
	private final float val;
	
	public FloatNode(Object value, Token token) {
		super(value, token);
		val = Float.parseFloat((String) value);
	}

	public float getVal() {
		return val;
	}
	
	@Override
	public String toString() {
		return "" + val;
	}
	
}
