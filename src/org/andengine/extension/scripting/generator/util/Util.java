package org.andengine.extension.scripting.generator.util;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:10:42 - 21.03.2012
 */
public class Util {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public static File getInJavaClassSourceFile(final File pInJavaRoot, final String pFullyQualifiedClassName) {
		return new File(pInJavaRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".java");
	}

	public static File getInJavaClassFile(final File pInJavaBinRootClasses, final String pFullyQualifiedClassName) {
		return new File(pInJavaBinRootClasses, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".class");
	}

	public static File getGenJavaClassSourceFile(final File pGenJavaRoot, final Class<?> pClass, final String pGenJavaClassSuffix) {
		return new File(pGenJavaRoot, Util.getGenJavaClassFullyQualifiedName(pClass).replace('.', File.separatorChar) + pGenJavaClassSuffix + ".java");
	}

	public static File getGenCppClassSourceFile(final File pGenCppRoot, final Class<?> pClass, final String pGenCppClassSuffix) {
		return new File(pGenCppRoot, pClass.getName().replace('.', File.separatorChar) + pGenCppClassSuffix + ".cpp");
	}

	public static File getGenCppClassHeaderFile(final File pGenCppRoot, final Class<?> pClass, final String pGenCppClassSuffix) {
		return new File(pGenCppRoot, pClass.getName().replace('.', File.separatorChar) + pGenCppClassSuffix + ".h");
	}

	public static String getVisibilityModifiersAsString(final AccessibleObject pAccessibleObject) {
		if(pAccessibleObject instanceof Constructor<?>) {
			return Util.getModifiersAsString(((Constructor<?>)pAccessibleObject).getModifiers());
		} else if(pAccessibleObject instanceof Method) {
			return Util.getModifiersAsString((((Method)pAccessibleObject)).getModifiers());
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static String getGenJavaClassName(final Class<?> pClass, final String pGenJavaClassSuffix) {
		return pClass.getSimpleName() + pGenJavaClassSuffix;
	}

	public static String getGenCppClassName(final Class<?> pClass, final String pGenCppClassSuffix) {
		return pClass.getSimpleName() + pGenCppClassSuffix;
	}

	public static String getGenJavaClassFullyQualifiedName(final Class<?> pClass) {
		return pClass.getName().replace("org.andengine", "org.andengine.extension.scripting");
	}

	public static String getGenJavaClassPackageName(final Class<?> pClass) {
		return pClass.getPackage().getName().replace("org.andengine", "org.andengine.extension.scripting");
	}

	public static String getJavaNativeMethodName(final Method pMethod) {
		return "native" + Util.capitalizeFirstCharacter(pMethod.getName());
	}

	public static String getCppJNIMethodFullyQualifiedName(final Method pMethod, final String pGenJavaClassSuffix) {
		return "Java_" + Util.getGenJavaClassPackageName(pMethod.getDeclaringClass()).replace('.', '_') + "_" + Util.getGenJavaClassName(pMethod.getDeclaringClass(), pGenJavaClassSuffix) + "_" + Util.getJavaNativeMethodName(pMethod);
	}

	public static String capitalizeFirstCharacter(final String methodName) {
		return Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1, methodName.length());
	}

	public static String getModifiersAsString(final int pModifiers) {
		final StringBuilder modifiersBuilder = new StringBuilder();

		if(Modifier.isPublic(pModifiers)) {
			modifiersBuilder.append("public");
		} else if(Modifier.isProtected(pModifiers)) {
			modifiersBuilder.append("protetcted");
		} else if(Modifier.isPrivate(pModifiers)) {
			modifiersBuilder.append("private");
		}

		return modifiersBuilder.toString();
	}

	public static String getMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes;
		if(pAccessibleObject instanceof Constructor<?>) {
			parameterTypes = ((Constructor<?>)pAccessibleObject).getParameterTypes();
		} else if(pAccessibleObject instanceof Method) {
			parameterTypes = ((Method)pAccessibleObject).getParameterTypes();
		} else {
			throw new IllegalArgumentException();
		}

		final BytecodeReadingParanamer bytecodeReadingParanamer = new BytecodeReadingParanamer();
		final String[] parameterNames = bytecodeReadingParanamer.lookupParameterNames(pAccessibleObject);
		
		return Util.getMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public static String getMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}

			// TODO Add import, when name != simplename. 
//			final String parameterTypeName = parameterType.getName();
			final String parameterTypeName = pParameterTypes[i].getSimpleName();
			
			stringBuilder.append("final").append(' ').append(parameterTypeName).append(' ').append(pParameterNames[i]);
		}

		return stringBuilder.toString();
	}

	public static String getMethodCallParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final BytecodeReadingParanamer bytecodeReadingParanamer = new BytecodeReadingParanamer();
		final String[] parameterNames = bytecodeReadingParanamer.lookupParameterNames(pAccessibleObject);
		
		return Util.getMethodCallParamatersAsString(parameterNames);
	}

	public static String getMethodCallParamatersAsString(final String[] pParameterNames) {
		if(pParameterNames.length == 0) {
			return null;
		}

		final StringBuilder stringBuilder = new StringBuilder();
		
		for(int i = 0; i < pParameterNames.length; i++) {
			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(pParameterNames[i]);
		}
		
		return stringBuilder.toString();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
