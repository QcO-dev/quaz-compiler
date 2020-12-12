package quaz.compiler.compiler;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;

public class Descriptors {
	
	public static boolean isInteger(String descriptor) {
		
		switch(descriptor) {
			
			case "I":
			case "Z":
			case "C":
			case "B":
			case "S":
				return true;
			
		}
		
		return false;
		
	}
	
	public static boolean descriptorIsArray(String descriptor) {
		return descriptor.startsWith("[");
	}
	
	public static int getArrayLengthFromDescriptor(String descriptor) {
		return getArrayLengthFromDescriptor(descriptor, 0);
	}
	
	public static int getArrayLengthFromDescriptor(String descriptor, int current) {
		
		int count = current;
		
		if(descriptor.startsWith("[")) {
			count++;
		}
		
		if(descriptor.substring(1).startsWith("[")) {
			return getArrayLengthFromDescriptor(descriptor.substring(1), count);
		}
		
		return count;
		
	}
	
	//TODO Multi dim. arrays
	public static String removeArrayFromDescriptor(String descriptor) {
		return descriptor.substring(descriptor.indexOf('[') + 1);
	}
	
	public static String removeArrayFromType(String descriptor) {
		
		return descriptor.substring(0, descriptor.lastIndexOf('['));
		
	}
	
	public static int primativeToOpcodeType(String descriptor) {
		
		switch(descriptor) {
			
			case "I": return Opcodes.T_INT;
			case "D": return Opcodes.T_DOUBLE;
			case "F": return Opcodes.T_FLOAT;
			case "Z": return Opcodes.T_BOOLEAN;
			case "C": return Opcodes.T_CHAR;
			case "B": return Opcodes.T_BYTE;
			case "J": return Opcodes.T_LONG;
			case "S": return Opcodes.T_SHORT;
		}
		
		return 0;
		
	}
	
	public static boolean isPrimative(String descriptor) {
		
		switch(descriptor) {
			
			case "I":
			case "D":
			case "F":
			case "Z":
			case "V":
			case "C":
			case "B":
			case "J":
			case "S":
				return true;
			
			default:
				return false;
		}
		
	}
	
	public static boolean typeIsPrimative(String type) {
		
		switch(type) {
			
			case "int":
			case "double":
			case "float":
			case "boolean":
			case "void":
			case "char":
			case "byte":
			case "long":
			case "short":
				return true;
			
			default:
				return false;
		}
		
	}
	
	public static String descriptorToType(String descriptor) {
			
		String type = "";
		
		switch(descriptor) {
			case "I": type = "int"; break;
			case "D": type = "double"; break;
			case "F": type = "float"; break;
			case "Z": type = "boolean"; break;
			case "C": type = "char"; break;
			case "B": type = "byte"; break;
			case "J": type = "long"; break;
			case "S": type = "short"; break;
			default:
				if(descriptor.startsWith("[")) {
					type = descriptorToType(descriptor.substring(1)) + "[]";
				}
				else {
					type = descriptor.substring(1, descriptor.length()-1).replace('/', '.');
				}
				break;
		}
		
		return type;
		
	}
	
	public static String typeToDescriptor(String type) {
		
		String descriptor = "";
		
		switch(type) {
			case "int": descriptor = "I"; break;
			case "double": descriptor = "D"; break;
			case "float": descriptor = "F"; break;
			case "boolean": descriptor = "Z"; break;
			case "void": descriptor = "V"; break;
			case "char": descriptor = "C"; break;
			case "byte": descriptor = "B"; break;
			case "long": descriptor = "J"; break;
			case "short": descriptor = "S"; break;
			default:
				if(type.endsWith("[]")) {
					descriptor = "[" + typeToDescriptor(type.substring(0, type.length()-2));
				}
				else {
					descriptor = type.replace('.', '/');
				}
				break;
		}
		
		return descriptor;
		
	}
	
	public static String typeToMethodDescriptor(String type) {
		String descriptor = "";
		
		switch(type) {
			case "int": descriptor = "I"; break;
			case "double": descriptor = "D"; break;
			case "float": descriptor = "F"; break;
			case "boolean": descriptor = "Z"; break;
			case "void": descriptor = "V"; break;
			case "char": descriptor = "C"; break;
			case "byte": descriptor = "B"; break;
			case "long": descriptor = "J"; break;
			case "short": descriptor = "S"; break;
			default:
				if(type.endsWith("[]")) {
					descriptor = "[" + typeToMethodDescriptor(type.substring(0, type.length()-2));
				}
				else {
					descriptor = "L" + type.replace('.', '/') + ";";
				}
				break;
		}
		
		return descriptor;
	}
	
	public static Class<?>[] descriptorToClasses(String descriptor) {
		
		ArrayList<Class<?>> classes = new ArrayList<>();
		
		String argDescriptors = descriptor.substring(1, descriptor.indexOf(')'));
		
		while(argDescriptors.length() != 0) {
			switch(argDescriptors.charAt(0)) {
				case 'L':
					classes.add(descriptorToClass(argDescriptors.substring(1, argDescriptors.indexOf(';'))));
					argDescriptors = argDescriptors.substring(argDescriptors.indexOf(';'));
					break;
				case 'I':
					classes.add(int.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'D':
					classes.add(double.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'F':
					classes.add(float.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'Z':
					classes.add(boolean.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'C':
					classes.add(char.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'B':
					classes.add(byte.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'J':
					classes.add(long.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				case 'S':
					classes.add(short.class);
					argDescriptors = argDescriptors.substring(1);
					break;
				default:
					argDescriptors = argDescriptors.substring(1); // Skip
					break;
			}
			
			//System.out.println(argDescriptors);
			//System.out.println(argDescriptors.length());
		
		}
		
		return classes.toArray(new Class<?>[] {});
	}
	
	public static Class<?> descriptorToClass(String descriptor) {
		
		try {
			return Class.forName(descriptor.replace('/', '.'));
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static List<String> descriptorToTypes(String descriptor) {
		
		ArrayList<String> types = new ArrayList<>();
		
		String argDescriptors = descriptor.substring(1, descriptor.indexOf(')'));
		
		while(argDescriptors.length() != 0) {
			switch(argDescriptors.charAt(0)) {
			case 'L':
				types.add(argDescriptors.substring(1, argDescriptors.indexOf(';')).replace('/', '.'));
				argDescriptors = argDescriptors.substring(argDescriptors.indexOf(';'));
				break;
			case 'I':
				types.add("int");
				argDescriptors = argDescriptors.substring(1);
				break;
			case 'D':
				types.add("double");
				argDescriptors = argDescriptors.substring(1);
				break;
			case 'F':
				types.add("float");
				argDescriptors = argDescriptors.substring(1);
				break;
			case 'Z':
				types.add("boolean");
				argDescriptors = argDescriptors.substring(1);
				break;
			case 'C':
				types.add("char");
				argDescriptors = argDescriptors.substring(1);
				break;
			case 'B':
				types.add("byte");
				argDescriptors = argDescriptors.substring(1);
				break;
				
			case 'J':
				types.add("long");
				argDescriptors = argDescriptors.substring(1);
				break;
				
			case 'S':
				types.add("short");
				argDescriptors = argDescriptors.substring(1);
				break;
				
			default:
				argDescriptors = argDescriptors.substring(1); // Skip
				break;
			}
			
			//System.out.println(argDescriptors);
			//System.out.println(argDescriptors.length());
		
		}
		
		return types;
		
	}
	
	public static String cropTypeDescriptor(String descriptor) {
		
		if(!descriptor.startsWith("L")) {
			return descriptor;
		}
		
		return descriptor.substring(1, descriptor.length()-1);
		
	}
	
	public static boolean isWide(String descriptor) {
		switch(descriptor) {
			case "D":
			case "J":
				return true;
			default:
				return false;
		}
	}
	
}
