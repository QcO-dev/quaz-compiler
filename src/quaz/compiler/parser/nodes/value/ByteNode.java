package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class ByteNode extends Node {
	
	byte val;
	
	public ByteNode(String value, Token token) {
		super(value, token);
		
		val = Byte.parseByte(value);
	}
	
	@Override
	public String toString() {
		return val + "b";
	}

	public byte getVal() {
		return val;
	}
	
	
}
