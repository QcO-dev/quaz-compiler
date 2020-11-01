package quaz.compiler.parser.nodes.function;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;
import quaz.compiler.standardLibrary.Pair;

public class ParameterListNode extends Node {

	private final Pair<Token, String>[] vars;
	
	@SuppressWarnings("unchecked")
	public ParameterListNode(Pair<?, ?>[] value, Position start, Position end) {
		super(value, start, end);
		vars = (Pair<Token, String>[]) value;
	}

	public Pair<Token, String>[] getVars() {
		return vars;
	}
	
	@Override
	public String toString() {
		
		String val = "";
		
		for(int i = 0; i < vars.length; i++) {
			val += (vars[i].getFirst() + " -> " + vars[i].getSecond()) + (i == vars.length-1 ? "" : ", ");
		}
		
		return "[" + val + "]";
	}
	
}
