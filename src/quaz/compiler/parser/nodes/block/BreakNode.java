package quaz.compiler.parser.nodes.block;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class BreakNode extends Node {

	public BreakNode(Token token) {
		super(null, token);
	}
	
	@Override
	public String toString() {
		return "break";
	}
	
}
