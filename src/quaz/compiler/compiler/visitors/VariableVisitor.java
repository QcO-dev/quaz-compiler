package quaz.compiler.compiler.visitors;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import quaz.compiler.compiler.Cast;
import quaz.compiler.compiler.Context;
import quaz.compiler.compiler.Descriptors;
import quaz.compiler.compiler.opStack.OperationStack;
import quaz.compiler.compiler.opStack.nodes.InsnNode;
import quaz.compiler.compiler.opStack.nodes.LabelNode;
import quaz.compiler.compiler.opStack.nodes.LineNumberNode;
import quaz.compiler.compiler.opStack.nodes.MethodNode;
import quaz.compiler.compiler.opStack.nodes.TypeNode;
import quaz.compiler.compiler.opStack.nodes.VarNode;
import quaz.compiler.compiler.values.LocalVariable;
import quaz.compiler.compiler.values.LocalVariables;
import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.lexer.Token;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.variable.VariableAccessNode;
import quaz.compiler.parser.nodes.variable.VariableDeclarationNode;

public class VariableVisitor {

	public void visitVariableDeclarationNode(Node node, Context context) throws CompilerLogicException {

		VariableDeclarationNode vdn = (VariableDeclarationNode) node;

		String name = vdn.getName();

		OperationStack stack = context.getOpStack();
		
		LocalVariables locals = context.getLocalVariables();
		
		// TODO ARRAYS
		String descriptor = Descriptors.typeToDescriptor(vdn.getTypeName());
		
		
		if(Descriptors.descriptorIsArray(descriptor)) {
			
			if(vdn.isDefault()) {
				stack.push(new InsnNode(Opcodes.ACONST_NULL));
			} else {
				context.getCompilerInstance().visit(vdn.getVal(), context);
				
				if(!context.getLastDescriptor().equals(descriptor)) {
					throw new CompilerLogicException(
							"Mismatched types in assignment. Expected " + vdn.getTypeName() + " but got "
									+ Descriptors.descriptorToType(context.getLastDescriptor()),
							vdn.getStart(), vdn.getEnd());
				}
			}
			
			stack.push(new VarNode(Opcodes.ASTORE, locals.getNextIndex()));
			
			locals.put(name, new LocalVariable(name, descriptor, locals.getNextIndex(), false));
			
			locals.incrementIndex();
			
			context.setLastWasConstant(false);
			
			return;
		}
		
		if(vdn.isExplicit()) {
			if(vdn.isKeyword()) {
				
				locals.put(name, new LocalVariable(name, descriptor, locals.getNextIndex(), true));
				
				// Writes the correct bytecode to get the value
				if(vdn.isDefault()) {

					switch(vdn.getTypeName()) {
						case "int":
						case "char":
						case "byte":
						case "boolean":
						case "short":
							stack.push(new InsnNode(Opcodes.ICONST_0));
							break;
						case "long":
							stack.push(new InsnNode(Opcodes.LCONST_0));
							break;
						case "double":
							stack.push(new InsnNode(Opcodes.DCONST_0));
							break;
						case "float":
							stack.push(new InsnNode(Opcodes.FCONST_0));
							break;
					}

				} else {

					Label l0 = new Label();
					stack.push(new LabelNode(l0));
					stack.push(new LineNumberNode(vdn.getVal().getStart().getLine(), l0));

					context.getCompilerInstance().visit(vdn.getVal(), context);

					if(!context.getLastDescriptor().equals(descriptor)) {
						
						try {
							Cast.primative(node, context.getLastDescriptor(), descriptor, stack, context);
						} catch(CompilerLogicException e) {
							throw new CompilerLogicException(
									"Mismatched types in assignment. Expected " + vdn.getTypeName() + " but got "
											+ Descriptors.descriptorToType(context.getLastDescriptor()),
									vdn.getStart(), vdn.getEnd());
						}
					}

				}

				// Stores the primitive in the correct index. Automatically uses the correct
				// bytecode instruction (i.e. istore_0 or istore 10)
				Label l0 = new Label();
				stack.push(new LabelNode(l0));
				stack.push(new LineNumberNode(vdn.getStart().getLine(), l0));

				generatePrimativeStoreValue(vdn.getTypeName(), locals.getNextIndex(), stack);
				
				if(Descriptors.isWide(descriptor)) {
					locals.incrementIndex();
				}
				
				context.setLastDescriptor(descriptor);

			}
			// Not a keyword -> a type
			else {

				String typeGiven = vdn.getTypeName();

				String type = typeGiven.contains("/") ? typeGiven : context.getTypeReferences().get(typeGiven);

				if(type == null) {
					throw new CompilerLogicException("Unknown type " + typeGiven + " used in current scope",
							vdn.getStart(), vdn.getEnd());
				}

				if(!vdn.isDefault()) {
					Label l0 = new Label();
					stack.push(new LabelNode(l0));
					stack.push(new LineNumberNode(vdn.getVal().getStart().getLine(), l0));
					context.getCompilerInstance().visit(vdn.getVal(), context);

					if(!context.getLastDescriptor().equals("L" + type + ";")) {
						
						try {
							Cast.nonPrimative(node, "L" + type + ";", context.getLastDescriptor(), stack, context);
						} catch(CompilerLogicException e) {
							throw new CompilerLogicException(
									"Mismatched types in assignment. Expected " + type.replace('/', '.') + " but got "
											+ Descriptors.descriptorToType(context.getLastDescriptor()),
									vdn.getStart(), vdn.getEnd());
						}
						
					}

				} else {
					// Constucts a new object of the above type
					stack.push(new TypeNode(Opcodes.NEW, type));
					stack.push(new InsnNode(Opcodes.DUP));
					stack.push(new MethodNode(Opcodes.INVOKESPECIAL, type, "<init>", "()V", false));
				}

				Label l1 = new Label();
				stack.push(new LabelNode(l1));
				stack.push(new LineNumberNode(vdn.getStart().getLine(), l1));
				stack.push(new VarNode(Opcodes.ASTORE, locals.getNextIndex()));

				locals.put(name, new LocalVariable(name, "L" + type + ";", locals.getNextIndex(), false));

				context.setLastDescriptor(type);

			}

		}

		else {

			if(vdn.isDefault()) {
				stack.push(new TypeNode(Opcodes.NEW, "java/lang/Object"));
				stack.push(new InsnNode(Opcodes.DUP));
				stack.push(new MethodNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));

				Label l1 = new Label();
				stack.push(new LabelNode(l1));
				stack.push(new LineNumberNode(vdn.getStart().getLine(), l1));
				stack.push(new VarNode(Opcodes.ASTORE, locals.getNextIndex()));

				locals.put(name, new LocalVariable(name, "Ljava/lang/Object;", locals.getNextIndex(), false));

				context.setLastDescriptor("java/lang/Object");

			}

			else {

				context.getCompilerInstance().visit(vdn.getVal(), context);

				String desc = context.getLastDescriptor();
				
				String type = Descriptors.descriptorToType(desc);
				
				if(Token.TYPE_KEYWORDS_ARRAY.contains(type)) {
					Label l0 = new Label();
					stack.push(new LabelNode(l0));
					stack.push(new LineNumberNode(vdn.getStart().getLine(), l0));
					
					generatePrimativeStoreValue(type, locals.getNextIndex(), stack);
					
					locals.put(name, new LocalVariable(name, desc, locals.getNextIndex(), true));
					if(Descriptors.isWide(desc)) {
						locals.incrementIndex();
					}
				} else {
					Label l0 = new Label();
					stack.push(new LabelNode(l0));
					stack.push(new LineNumberNode(vdn.getStart().getLine(), l0));
					stack.push(new VarNode(Opcodes.ASTORE, locals.getNextIndex()));
					
					locals.put(name, new LocalVariable(name, desc, locals.getNextIndex(), false));
				}
				
				context.setLastDescriptor(desc);
			}

		}
		
		locals.incrementIndex();
		
		context.setLastWasConstant(false);

	}

	public static void generatePrimativeStoreValue(String type, int index, OperationStack stack) {
		switch(type) {
			case "char":
			case "byte":
			case "int":
			case "short":
				stack.push(new VarNode(Opcodes.ISTORE, index));
				break;
			case "long":
				stack.push(new VarNode(Opcodes.LSTORE, index));
				break;
			case "double":
				stack.push(new VarNode(Opcodes.DSTORE, index));
				break;
			case "float":
				stack.push(new VarNode(Opcodes.FSTORE, index));
				break;
			case "boolean":
				stack.push(new VarNode(Opcodes.ISTORE, index));
				break;
		}
	}

	public void visitVariableAccessNode(Node node, Context context) throws CompilerLogicException {

		String name = ((VariableAccessNode) node).getName();

		LocalVariable var = context.getLocalVariables().get(name);

		if(var == null) {
			if(context.getTypeReferences().containsKey(name)) {
				context.setLastDescriptor("L" + name + ";");
				return;
			}
			throw new CompilerLogicException("Variable \'" + name + "\' does not exist in the current scope.",
					node.getStart(), node.getEnd());
		}

		OperationStack stack = context.getOpStack();
		
		Label l0 = new Label();
		stack.push(new LabelNode(l0));
		stack.push(new LineNumberNode(node.getStart().getLine(), l0));
		if(var.isPrimitive()) {

			switch(var.getDescriptor()) {
			case "I":
				stack.push(new VarNode(Opcodes.ILOAD, var.getIndex()));
				context.setLastDescriptor("I");
				break;
			case "C":
				stack.push(new VarNode(Opcodes.ILOAD, var.getIndex()));
				context.setLastDescriptor("C");
				break;
			case "B":
				stack.push(new VarNode(Opcodes.ILOAD, var.getIndex()));
				context.setLastDescriptor("B");
				break;
			case "S":
				stack.push(new VarNode(Opcodes.ILOAD, var.getIndex()));
				context.setLastDescriptor("S");
				break;
			case "J":
				stack.push(new VarNode(Opcodes.LLOAD, var.getIndex()));
				context.setLastDescriptor("J");
				break;
			case "D":
				stack.push(new VarNode(Opcodes.DLOAD, var.getIndex()));
				context.setLastDescriptor("D");
				break;
			case "F":
				stack.push(new VarNode(Opcodes.FLOAD, var.getIndex()));
				context.setLastDescriptor("F");
				break;
			case "Z":
				stack.push(new VarNode(Opcodes.ILOAD, var.getIndex()));
				context.setLastDescriptor("Z");
				break;
			}

		} else {
			stack.push(new VarNode(Opcodes.ALOAD, var.getIndex()));
			context.setLastDescriptor(var.getDescriptor());
		}
		
		context.setLastWasConstant(false);
	}

}
