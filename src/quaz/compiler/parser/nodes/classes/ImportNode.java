package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class ImportNode extends Node {

	private final String typeName;
	
	public ImportNode(String value, Position start, Position end) {
		super(value, start, end);
		this.typeName = value;
	}

	public String getTypeName() {
		return typeName;
	}
	
	@Override
	public String toString() {
		return "import " + typeName + ";";
	}

}
