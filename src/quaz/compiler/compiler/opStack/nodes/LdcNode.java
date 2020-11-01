package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;

public class LdcNode extends OpNode {
	
	Object value;
	
	public LdcNode(Object value) {
		super(0);
		this.value = value;
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitLdcInsn(value);
	}
	
	// The getValue method is available in InsnNode, IntInsnNode and LdcNode. It returns the known constant value. It should be used along with Context#getLastWasConstant()
	public Object getValue() {
		return value;
	}
	
}
