package quaz.compiler.compiler.opStack.nodes;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InsnNode extends OpNode {

	public InsnNode(int opcode) {
		super(opcode);
	}

	@Override
	public void generateBytecode(MethodVisitor mv) {
		mv.visitInsn(getOpcode());
	}
	
	
	public Object getValue() {
		
		switch(opcode) {
			case Opcodes.ICONST_0: return 0;
			case Opcodes.ICONST_1: return 1;
			case Opcodes.ICONST_2: return 2;
			case Opcodes.ICONST_3: return 3;
			case Opcodes.ICONST_4: return 4;
			case Opcodes.ICONST_5: return 5;
			case Opcodes.ICONST_M1: return -6;
			
			case Opcodes.DCONST_0: return 0.0;
			case Opcodes.DCONST_1: return 1.0;
			
			case Opcodes.FCONST_0: return 0f;
			case Opcodes.FCONST_1: return 1f;
			case Opcodes.FCONST_2: return 2f;
			
		}
		
		return opcode;
		
	}
	
}
