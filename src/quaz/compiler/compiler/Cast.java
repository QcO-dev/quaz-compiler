package quaz.compiler.compiler;

import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.MethodNode;
import quaz.compiler.compiler.opStack.nodes.TypeNode;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;

public class Cast {
	
	public static void primative(Node node, String leftDesc, String desc, OperationStack stack, Context context) throws CompilerLogicException {
		switch(leftDesc) {
			case "I":
				switch(desc) {
					case "I":
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.I2D));
						context.setLastDescriptor("D");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.I2F));
						context.setLastDescriptor("F");
						break;
					case "B":
						stack.push(new InsnNode(Opcodes.I2B));
						context.setLastDescriptor("B");
						break;
					case "Z":
						context.setLastDescriptor("Z");
						break;
					case "C":
						stack.push(new InsnNode(Opcodes.I2C));
						context.setLastDescriptor("C");
						break;
					case "S":
						stack.push(new InsnNode(Opcodes.I2S));
						context.setLastDescriptor("S");
						break;
					case "J":
						stack.push(new InsnNode(Opcodes.I2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
					default:
						throw new CompilerLogicException("Cannot cast from int to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
				
			case "C":
				switch(desc) {
					case "C":
						break;
					case "I":
						context.setLastDescriptor("I");
						break;
					case "B":
						stack.push(new InsnNode(Opcodes.I2B));
						context.setLastDescriptor("B");
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.I2D));
						context.setLastDescriptor("D");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.I2F));
						context.setLastDescriptor("F");
						break;
					case "Z":
						context.setLastDescriptor("Z");
						break;
					case "S":
						stack.push(new InsnNode(Opcodes.I2S));
						context.setLastDescriptor("S");
						break;
					
					case "J":
						stack.push(new InsnNode(Opcodes.I2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
					default:
						throw new CompilerLogicException("Cannot cast from char to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;	
			
			case "D":
				switch(desc) {
					case "D":
						break;
					case "C":
					case "B":
					case "Z":
					case "I":
						stack.push(new InsnNode(Opcodes.D2I));
						context.setLastDescriptor("I");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.D2F));
						context.setLastDescriptor("F");
						break;
					case "J":
						stack.push(new InsnNode(Opcodes.D2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
						
					default:
						throw new CompilerLogicException("Cannot cast from double to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
			case "F":
				switch(desc) {
					case "F":
						break;
					case "C":
					case "Z":
					case "B":
					case "I":
						stack.push(new InsnNode(Opcodes.F2I));
						context.setLastDescriptor("I");
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.F2D));
						context.setLastDescriptor("D");
						break;
						
					case "J":
						stack.push(new InsnNode(Opcodes.F2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
						
					default:
						throw new CompilerLogicException("Cannot cast from float to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
			case "Z":
				switch(desc) {
					case "Z":
						break;
					case "I":
						context.setLastDescriptor("I");
						break;
						
					case "S":
						stack.push(new InsnNode(Opcodes.I2S));
						context.setLastDescriptor("S");
						break;
						
					case "C":
						stack.push(new InsnNode(Opcodes.I2C));
						context.setLastDescriptor("C");
						break;
						
					case "B":
						stack.push(new InsnNode(Opcodes.I2B));
						context.setLastDescriptor("B");
						break;
						
					case "J":
						stack.push(new InsnNode(Opcodes.I2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
						
					default:
						throw new CompilerLogicException("Cannot cast from boolean to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
				
			case "B":
				switch(desc) {
					case "B":
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.I2D));
						context.setLastDescriptor("D");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.I2F));
						context.setLastDescriptor("F");
						break;
					case "I":
						context.setLastDescriptor("I");
						break;
					case "Z":
						context.setLastDescriptor("Z");
						break;
					case "C":
						context.setLastDescriptor("C");
						break;
					case "S":
						context.setLastDescriptor("S");
						break;
					case "J":
						stack.push(new InsnNode(Opcodes.I2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
					default:
						throw new CompilerLogicException("Cannot cast from byte to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
				
			case "S":
				switch(desc) {
					case "S":
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.I2D));
						context.setLastDescriptor("D");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.I2F));
						context.setLastDescriptor("F");
						break;
					case "I":
						context.setLastDescriptor("I");
						break;
					case "Z":
						context.setLastDescriptor("Z");
						break;
					case "C":
						context.setLastDescriptor("C");
						break;
					case "B":
						context.setLastDescriptor("B");
						break;
					case "J":
						stack.push(new InsnNode(Opcodes.I2L));
						context.setLastDescriptor("J");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
					default:
						throw new CompilerLogicException("Cannot cast from short to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
				
			case "J":
				switch(desc) {
					case "J":
						break;
					case "D":
						stack.push(new InsnNode(Opcodes.L2D));
						context.setLastDescriptor("D");
						break;
					case "F":
						stack.push(new InsnNode(Opcodes.L2F));
						context.setLastDescriptor("F");
						break;
					case "I":
						stack.push(new InsnNode(Opcodes.L2I));
						context.setLastDescriptor("I");
						break;
					case "Z":
						stack.push(new InsnNode(Opcodes.L2I));
						context.setLastDescriptor("Z");
						break;
					case "C":
						stack.push(new InsnNode(Opcodes.L2I));
						context.setLastDescriptor("C");
						break;
					case "S":
						stack.push(new InsnNode(Opcodes.L2I));
						context.setLastDescriptor("S");
						break;
					case "B":
						stack.push(new InsnNode(Opcodes.L2I));
						context.setLastDescriptor("S");
						break;
						
					case "Ljava/lang/String;": {
						stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
						context.setLastDescriptor("Ljava/lang/String;");
						break;
					}
					default:
						throw new CompilerLogicException("Cannot cast from byte to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
				}
				break;
				
		}
		
		context.setLastWasConstant(false);
	}
	
	public static void nonPrimative(Node node, String leftDesc, String desc, OperationStack stack, Context context) throws CompilerLogicException {
		Class<?> leftClass =  Descriptors.descriptorToClass(Descriptors.descriptorToType(leftDesc));
		
		if(leftClass.isAssignableFrom(String.class)) {
			switch(desc) {
				case "I": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false));
					context.setLastDescriptor("I");
					context.setLastWasConstant(false);
					return;
				}
				case "C": {
					stack.push(new InsnNode(Opcodes.ICONST_0));
					stack.push(new MethodNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false));
					context.setLastDescriptor("C");
					context.setLastWasConstant(false);
					return;
				}
				
				case "B": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "parseByte", "(Ljava/lang/String;)B", false));
					context.setLastDescriptor("B");
					context.setLastWasConstant(false);
					return;
				}
				
				case "S": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Short", "parseShort", "(Ljava/lang/String;)S", false));
					context.setLastDescriptor("B");
					context.setLastWasConstant(false);
					return;
				}
				
				case "D": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D", false));
					context.setLastDescriptor("D");
					context.setLastWasConstant(false);
					return;
				}
				
				case "F": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false));
					context.setLastDescriptor("F");
					context.setLastWasConstant(false);
					return;
				}
				
				case "Z": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false));
					context.setLastDescriptor("Z");
					context.setLastWasConstant(false);
					return;
				}
				
				case "J": {
					stack.push(new MethodNode(Opcodes.INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J", false));
					context.setLastDescriptor("Z");
					context.setLastWasConstant(false);
					return;
				}
					
			}
			
		}
		
		if(Descriptors.isPrimitive(desc)) {
			throw new CompilerLogicException("Cannot cast from " + Descriptors.descriptorToType(leftDesc) + " to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
		}
		
		Class<?> rightClass = Descriptors.descriptorToClass(Descriptors.descriptorToType(desc));
		
		if(!leftClass.isAssignableFrom(rightClass)) {
			throw new CompilerLogicException("Cannot cast from " + Descriptors.descriptorToType(leftDesc) + " to " + Descriptors.descriptorToType(desc), node.getStart(), node.getEnd());
		}
		
		stack.push(new TypeNode(Opcodes.CHECKCAST, Descriptors.cropTypeDescriptor(desc)));
		context.setLastDescriptor(desc);
	}
	
}
