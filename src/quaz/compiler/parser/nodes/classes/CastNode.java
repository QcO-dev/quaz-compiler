package quaz.compiler.parser.nodes.classes;

import quaz.compiler.parser.nodes.Node;
import quaz.compiler.position.Position;

public class CastNode extends Node {
	
	private final Node orginal;
	private final String castType;
	
	public CastNode(Node value, String castType, Position start, Position end) {
		super(value, start, end);
		this.orginal = value;
		this.castType = castType;
	}
	
	public Node getOrginal() {
		return orginal;
	}

	public String getCastType() {
		return castType;
	}

	@Override
	public String toString() {
		return orginal + " as " + castType;
	}
	
}
