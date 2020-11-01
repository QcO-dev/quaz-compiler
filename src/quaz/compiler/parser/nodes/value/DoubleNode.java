package quaz.compiler.parser.nodes.value;

import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;

public class DoubleNode extends Node {

	double val;
	
	public DoubleNode(String value, Token token) {
		super(value, token);
		
		val = Double.parseDouble(value);
	}

	public double getVal() {
		return val;
	}

	public void setVal(double val) {
		this.val = val;
	}

	@Override
	public String toString() {
		return "" + val;
	}
	
}
