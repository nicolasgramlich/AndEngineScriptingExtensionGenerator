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

	public static String uncapitalizeFirstCharacter(final String methodName) {
		return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1, methodName.length());
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

	public static String getJavaMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = Util.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = Util.getParameterNames(pAccessibleObject);

		return Util.getJavaMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public static String getJavaMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			final String parameterName = pParameterNames[i];
			// TODO Add import, when name != simplename.
//			final String parameterTypeName = parameterType.getName();
			final String parameterTypeName = pParameterTypes[i].getSimpleName();

			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}

			stringBuilder.append("final").append(' ').append(parameterTypeName).append(' ').append(parameterName);
		}

		return stringBuilder.toString();
	}

	public static String getJavaMethodCallParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final String[] parameterNames = Util.getParameterNames(pAccessibleObject);

		return Util.getJavaMethodCallParamatersAsString(parameterNames);
	}

	public static String getJavaMethodCallParamatersAsString(final String[] pParameterNames) {
		if(pParameterNames.length == 0) {
			return null;
		}

		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterNames.length; i++) {
			final String parameterName = pParameterNames[i];
			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(parameterName);
		}

		return stringBuilder.toString();
	}

	public static String getJNIExportMethodHeaderParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = Util.getParameterTypes(pAccessibleObject);

		return Util.getJNIExportMethodHeaderParamatersAsString(parameterTypes);
	}

	public static String getJNIExportMethodHeaderParamatersAsString(final Class<?>[] pParameterTypes) {
		final StringBuilder stringBuilder = new StringBuilder("JNIEnv*, jobject, jlong");

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = Util.getJNIParameterTypeName(parameterType);

			stringBuilder.append(", ").append(parameterTypeName);
		}

		return stringBuilder.toString();
	}

	public static String getJNIExportMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = Util.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = Util.getParameterNames(pAccessibleObject);

		return Util.getJNIExportMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public static String getJNIExportMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		final StringBuilder stringBuilder = new StringBuilder("JNIEnv* pJNIEnv, jobject pJObject, jlong pAddress");

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = Util.getJNIParameterTypeName(parameterType);
			final String parameterName = pParameterNames[i];

			stringBuilder.append(", ").append(parameterTypeName).append(' ').append(parameterName);
		}

		return stringBuilder.toString();
	}

	private static String getJNIParameterTypeName(final Class<?> parameterType) {
		final String parameterTypeName;
		if(parameterType == Byte.TYPE) {
			parameterTypeName = "jbyte";
		} else if(parameterType == Character.TYPE) {
			parameterTypeName = "jchar";
		} else if(parameterType == Short.TYPE) {
			parameterTypeName = "jshort";
		} else if(parameterType == Integer.TYPE) {
			parameterTypeName = "jint";
		} else if(parameterType == Long.TYPE) {
			parameterTypeName = "jlong";
		} else if(parameterType == Float.TYPE) {
			parameterTypeName = "jfloat";
		} else if(parameterType == Double.TYPE) {
			parameterTypeName = "jdouble";
		} else {
			parameterTypeName = "jobject";
		}
		return parameterTypeName;
	}

	private static String[] getParameterNames(final AccessibleObject pAccessibleObject) {
		final BytecodeReadingParanamer bytecodeReadingParanamer = new BytecodeReadingParanamer();
		return bytecodeReadingParanamer.lookupParameterNames(pAccessibleObject);
	}

	private static Class<?>[] getParameterTypes(final AccessibleObject pAccessibleObject) {
		if(pAccessibleObject instanceof Constructor<?>) {
			return ((Constructor<?>)pAccessibleObject).getParameterTypes();
		} else if(pAccessibleObject instanceof Method) {
			return ((Method)pAccessibleObject).getParameterTypes();
		} else {
			throw new IllegalArgumentException();
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
