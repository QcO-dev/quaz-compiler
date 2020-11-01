package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class PackageNode extends Node {

	private final String packageName;
	
	public PackageNode(String value, Position start, Position end) {
		super(value, start, end);
		packageName = value;
	}

	public String getPackageName() {
		return packageName;
	}
	
	@Override
	public String toString() {
		return "package " + packageName + "; ";
	}

}
