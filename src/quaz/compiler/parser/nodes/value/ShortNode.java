package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class ShortNode extends Node {

	short val;
	
	public ShortNode(String value, Token token) {
		super(value, token);
		val = Short.parseShort(value);
	}
	
	@Override
	public String toString() {
		return val + "s";
	}

	public short getVal() {
		return val;
	}
	
}
