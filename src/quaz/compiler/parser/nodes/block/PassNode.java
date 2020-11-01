package quaz.compiler.parser.nodes.block;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class PassNode extends Node {

	public PassNode(Token token) {
		super(null, token);
	}
	
	@Override
	public String toString() {
		return "pass";
	}
	
}
