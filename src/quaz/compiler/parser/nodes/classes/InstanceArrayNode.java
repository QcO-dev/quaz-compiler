package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class InstanceArrayNode extends Node {
	
	private final String typeName;
	private final Node lengthExpr;
	private final Node[] initList;
	
	public InstanceArrayNode(String typeName, Node lengthExpr, Node[] initList, Position start, Position end) {
		super(typeName, start, end);
		this.typeName = typeName;
		this.lengthExpr = lengthExpr;
		this.initList = initList;
	}

	public String getTypeName() {
		return typeName;
	}

	public Node getLengthExpr() {
		return lengthExpr;
	}

	public Node[] getInitList() {
		return initList;
	}
	
	@Override
	public String toString() {
		return "new " + typeName;
	}

}
