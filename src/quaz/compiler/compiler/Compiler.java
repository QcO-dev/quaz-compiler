package quaz.compiler.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import quaz.compiler.exception.CompilerLogicException;
import quaz.compiler.parser.nodes.Node;
import quaz.compiler.parser.nodes.classes.PackageNode;
import quaz.compiler.standardLibrary.Pair;

public class Compiler implements Opcodes {

	public Pair<String, byte[]> compile(Node ast) throws CompilerLogicException {
		
		// Let ASM calculate frame and max sizes (90% slower but easier to implement so for now use this)
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		
		String packageName = null;
		
		for(Node n : (Node[]) ast.getValue()) {
			if(n instanceof PackageNode) {
				packageName = ((PackageNode) n).getPackageName();
			}
		}
		
		String fileName = ast.getStart().getFile().getName();
		
		String name = fileName.substring(0, fileName.indexOf("."));
		
		String className = (packageName == null ? "" : packageName + ".") + name;
		
		String internalName = className.replace(".", "/");
		
		initWriter(writer, ast, internalName);
		
		Context context = new Context(this, writer, internalName);
		
		visitPreprocessors(ast, context);
		visit(ast, context);
		
		writer.visitEnd();
		
		return new Pair<String, byte[]>(className, writer.toByteArray());
		
	}
	
	private void initWriter(ClassWriter writer, Node ast, String name) {
		
		
		// Creates a public class with the correct name, which is a sub-class of java.lang.Object and is in Java 9 format
		
		writer.visit(V9, ACC_PUBLIC | ACC_SUPER, name, null, "java/lang/Object", null);
		
		writer.visitSource(ast.getStart().getFile().getName(), null);
		
		// Creates a constructor and executes the java.lang.Object constructor
		MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(1, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		//TODO
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		
	}
	
	public void visit(Node ast, Context context) throws CompilerLogicException {
		
		try {
		
			String className = ast.getClass().getName();
			
			String[] packageNameSplit = className.split("\\.");
			
			String packageName = packageNameSplit[packageNameSplit.length-2];
			
			String visitorName = packageName.substring(0, 1).toUpperCase() + packageName.substring(1) + "Visitor";
		
			Class<?> visitorClass = Class.forName("quaz.compiler.compiler.visitors." + visitorName);
			
			String simpleName = ast.getClass().getSimpleName();
			
			if(simpleName.equals("FunctionCallNode") || simpleName.equals("MemberAccessNode")) {
				Method method = visitorClass.getMethod("visit" + simpleName, Node.class, Context.class, boolean.class);
				
				method.invoke(visitorClass.getDeclaredConstructor().newInstance(), ast, context, false);
			}
			else {
				Method method = visitorClass.getMethod("visit" + simpleName, Node.class, Context.class);
				
				method.invoke(visitorClass.getDeclaredConstructor().newInstance(), ast, context);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CompilerLogicException) {
				throw (CompilerLogicException)e.getCause();
			}
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	
public void visit(Node ast, Context context, boolean root) throws CompilerLogicException {
		
		try {
		
			String className = ast.getClass().getName();
			
			String[] packageNameSplit = className.split("\\.");
			
			String packageName = packageNameSplit[packageNameSplit.length-2];
			
			String visitorName = packageName.substring(0, 1).toUpperCase() + packageName.substring(1) + "Visitor";
		
			Class<?> visitorClass = Class.forName("quaz.compiler.compiler.visitors." + visitorName);
			
			String simpleName = ast.getClass().getSimpleName();
			
			if(simpleName.equals("FunctionCallNode") || simpleName.equals("MemberAccessNode")) {
				Method method = visitorClass.getMethod("visit" + simpleName, Node.class, Context.class, boolean.class);
				
				method.invoke(visitorClass.getDeclaredConstructor().newInstance(), ast, context, root);
			}
			else {
				Method method = visitorClass.getMethod("visit" + simpleName, Node.class, Context.class);
				
				method.invoke(visitorClass.getDeclaredConstructor().newInstance(), ast, context);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CompilerLogicException) {
				throw (CompilerLogicException)e.getCause();
			}
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
	
	public void visitPreprocessors(Node ast, Context context) throws CompilerLogicException {
		
		try {
		
			String className = ast.getClass().getName();
			
			String[] packageNameSplit = className.split("\\.");
			
			String packageName = packageNameSplit[packageNameSplit.length-2];
			
			String visitorName = packageName.substring(0, 1).toUpperCase() + packageName.substring(1) + "Visitor";
		
			Class<?> visitorClass = Class.forName("quaz.compiler.compiler.preprocessors." + visitorName);
			
			Method method = visitorClass.getMethod("visit" + ast.getClass().getSimpleName(), Node.class, Context.class);
			
			method.invoke(visitorClass.getDeclaredConstructor().newInstance(), ast, context);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof CompilerLogicException) {
				throw (CompilerLogicException)e.getCause();
			}
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		
	}
	
}
