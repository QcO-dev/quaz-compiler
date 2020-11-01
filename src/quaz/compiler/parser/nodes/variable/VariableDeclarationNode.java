package quaz.compiler.parser.nodes.variable;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class VariableDeclarationNode extends Node {

	private String name;
	private String typeName;
	private Node val;
	private boolean isExplicit;
	private boolean isKeyword;
	private boolean isDefault;
	
	public VariableDeclarationNode(String name, String typeName, Node value, boolean isExplicit, boolean isKeyword, boolean isDefault, Position start, Position end) {
		super(name, start, end);
		this.name = name;
		this.typeName = typeName;
		val = value;
		this.isExplicit = isExplicit;
		this.isKeyword = isKeyword;
		this.isDefault = isDefault;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Node getVal() {
		return val;
	}

	public void setVal(Node value) {
		this.val = value;
	}

	public boolean isExplicit() {
		return isExplicit;
	}

	public void setExplicit(boolean isExplicit) {
		this.isExplicit = isExplicit;
	}

	public boolean isKeyword() {
		return isKeyword;
	}

	public void setKeyword(boolean isKeyword) {
		this.isKeyword = isKeyword;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	@Override
	public String toString() {
		return "var " + name + " = " + val;
	}
	
}
