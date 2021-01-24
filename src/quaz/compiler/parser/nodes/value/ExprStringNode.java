package quaz.compiler.parser.nodes.value;

import java.util.List;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class ExprStringNode extends Node {
	
	private final Node[] exprs;
	private final String rep;
	
	public ExprStringNode(String value, Node[] exprs, Token token) {
		super(value, token.getStart(), token.getEnd());
		this.exprs = exprs;
		this.rep = value;
	}

	public String getRep() {
		return rep;
	}

	public Node[] getExprs() {
		return exprs;
	}
	
	@Override
	public String toString() {
		String val = rep;
		
		List<Node> exprsList = List.of(exprs);
		
		for(Node expr : exprsList) {
			val = val.replaceFirst("\u0001", "${" + expr.toString() + "}");
		}
		
		return val;
	}
	
}
