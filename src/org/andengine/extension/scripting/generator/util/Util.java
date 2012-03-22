package org.andengine.extension.scripting.generator.util;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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

	public static File getGenCppClassSourceFile(final File pGenCppRoot, final Class<?> pClass) {
		return new File(pGenCppRoot, pClass.getName().replace('.', File.separatorChar) + ".cpp");
	}

	public static File getGenCppClassHeaderFile(final File pGenCppRoot, final Class<?> pClass) {
		return new File(pGenCppRoot, pClass.getName().replace('.', File.separatorChar) + ".h");
	}

	public static String getConstructorModifiersAsString(final Constructor<?> pConstructor) {
		return Util.getModifiersAsString(pConstructor.getModifiers());
	}

	public static String getMethodModifiersAsString(final Method pMethod) {
		return Util.getModifiersAsString(pMethod.getModifiers());
	}

	public static String getGenJavaClassName(final Class<?> pClass, final String pGenJavaClassSuffix) {
		return pClass.getSimpleName() + pGenJavaClassSuffix;
	}

	public static String getGenCppClassName(final Class<?> pClass) {
		return pClass.getSimpleName();
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

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
