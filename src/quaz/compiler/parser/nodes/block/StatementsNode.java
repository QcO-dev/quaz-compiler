package quaz.compiler.parser.nodes.block;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class StatementsNode extends Node {

	Node[] value;
	
	public StatementsNode(Node[] value, Position start, Position end) {
		super(value, start, end);
		this.value = value;
	}
	
	public String toString() {
		
		String val = "";
		
		for(int i = 0; i < value.length; i++) {
			
			val += value[i] + "; ";
			
		}
		
		return val;
	}
	
	
	
}
