package org.andengine.extension.scripting.generator.util;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;

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

	private final String mProxyJavaClassSuffix;
	private final String mProxyCppClassSuffix;
	private final List<String> mProxyMethodsInclude;
	private final List<String> mProxyClassesExclude;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Util(final String pGenJavaClassSuffix, final String pGenCppClassSuffix, final List<String> pGenMethodsInclude, final List<String> pGenClassesExclude) {
		this.mProxyJavaClassSuffix = pGenJavaClassSuffix;
		this.mProxyCppClassSuffix = pGenCppClassSuffix;
		this.mProxyMethodsInclude = pGenMethodsInclude;
		this.mProxyClassesExclude = pGenClassesExclude;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public File getInJavaClassSourceFile(final File pInJavaRoot, final String pFullyQualifiedClassName) {
		return new File(pInJavaRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".java");
	}

	public File getInJavaClassFile(final File pInJavaBinRootClasses, final String pFullyQualifiedClassName) {
		return new File(pInJavaBinRootClasses, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".class");
	}

	public File getGenJavaClassSourceFile(final File pProxyJavaRoot, final Class<?> pClass) {
		return new File(pProxyJavaRoot, this.getGenJavaClassFullyQualifiedName(pClass).replace('.', File.separatorChar) + this.mProxyJavaClassSuffix + ".java");
	}

	public File getGenCppClassSourceFile(final File pProxyCppRoot, final Class<?> pClass) {
		return new File(pProxyCppRoot, pClass.getName().replace('.', File.separatorChar) + this.mProxyCppClassSuffix + ".cpp");
	}

	public File getGenCppClassHeaderFile(final File pProxyCppRoot, final Class<?> pClass) {
		return new File(pProxyCppRoot, pClass.getName().replace('.', File.separatorChar) + this.mProxyCppClassSuffix + ".h");
	}

	public String getGenCppClassInclude(final Class<?> pClass) {
		return this.getGenCppClassInclude(pClass, false);
	}

	public String getGenCppClassInclude(final Class<?> pClass, final boolean pIgnoreSuffix) {
		return "#include \"src/" + this.getGenCppFullyQualifiedClassName(pClass, pIgnoreSuffix) + ".h\"";
	}

	public String getGenCppFullyQualifiedClassName(final Class<?> pClass) {
		return this.getGenCppFullyQualifiedClassName(pClass, false);
	}

	public String getGenCppFullyQualifiedClassName(final Class<?> pClass, final boolean pIgnoreSuffix) {
		return pClass.getName().replace('.', '/').replace('&', '/') + ((pIgnoreSuffix) ? "" : this.mProxyCppClassSuffix);
	}

	public String getGenJavaClassImport(final Class<?> pClass) {
		if(pClass.isArray()) {
			return this.getGenJavaClassImport(pClass.getComponentType());
		} else {
			return "import " + pClass.getName().replace('$', '.') + ";";
		}
	}

	public String getVisibilityModifiersAsString(final AccessibleObject pAccessibleObject) {
		if(pAccessibleObject instanceof Constructor<?>) {
			return this.getModifiersAsString(((Constructor<?>)pAccessibleObject).getModifiers());
		} else if(pAccessibleObject instanceof Method) {
			return this.getModifiersAsString((((Method)pAccessibleObject)).getModifiers());
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getGenJavaClassName(final Class<?> pClass) {
		return pClass.getSimpleName() + this.mProxyJavaClassSuffix;
	}

	public String getGenCppClassName(final Class<?> pClass) {
		return pClass.getSimpleName() + this.mProxyCppClassSuffix;
	}

	public String getGenCppStaticClassMemberName(final Class<?> pClass) {
		return this.getGenCppStaticClassMemberName(pClass, false);
	}

	public String getGenCppStaticClassMemberName(final Class<?> pClass, final boolean pIgnoreSuffix) {
		return "s" + pClass.getSimpleName() + ((pIgnoreSuffix) ? "" : this.mProxyCppClassSuffix) + "Class";
	}

	public String getGenJavaClassFullyQualifiedName(final Class<?> pClass) {
		return pClass.getName().replace("org.andengine", "org.andengine.extension.scripting");
	}

	public String getGenJavaClassPackageName(final Class<?> pClass) {
		return pClass.getPackage().getName().replace("org.andengine", "org.andengine.extension.scripting");
	}

	public String getJavaNativeMethodName(final Method pMethod) {
		return this.getJavaNativeMethodName(pMethod.getName());
	}

	public String getJavaNativeMethodName(final String pMethodName) {
		return "native" + this.capitalizeFirstCharacter(pMethodName);
	}

	public String getJNIExportMethodName(final Class<?> pClass, final Method pMethod) {
		return this.getJNIExportMethodName(pClass, pMethod.getName());
	}

	public String getJNIExportMethodName(final Class<?> pClass, final String pMethodName) {
		return "Java_" + this.getGenJavaClassPackageName(pClass).replace('.', '_') + "_" + this.getGenJavaClassName(pClass) + "_" + this.getJavaNativeMethodName(pMethodName);
	}

	public String capitalizeFirstCharacter(final String pString) {
		return Character.toUpperCase(pString.charAt(0)) + pString.substring(1, pString.length());
	}

	public String uncapitalizeFirstCharacter(final String pString) {
		return Character.toLowerCase(pString.charAt(0)) + pString.substring(1, pString.length());
	}

	public String getModifiersAsString(final int pModifiers) {
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

	public String getJavaMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getJavaMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public String getJavaMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
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

	public String getJavaMethodCallParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getJavaMethodCallParamatersAsString(parameterNames);
	}

	public String getJavaMethodCallParamatersAsString(final String[] pParameterNames) {
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

	public String getJNIExportMethodHeaderParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);

		return this.getJNIExportMethodHeaderParamatersAsString(parameterTypes);
	}

	public String getJNIExportMethodHeaderParamatersAsString(final Class<?>[] pParameterTypes) {
		final StringBuilder stringBuilder = new StringBuilder("JNIEnv*, jobject, jlong");

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = this.getJNIParameterTypeName(parameterType);

			stringBuilder.append(", ").append(parameterTypeName);
		}

		return stringBuilder.toString();
	}

	public String getGenCppMethodHeaderParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);

		return this.getGenCppMethodHeaderParamatersAsString(parameterTypes);
	}

	public String getGenCppMethodHeaderParamatersAsString(final Class<?>[] pParameterTypes) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = this.getGenCppParameterTypeName(parameterType);

			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(parameterTypeName);
		}

		return stringBuilder.toString();
	}

	public String getGenCppMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getGenCppMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public String getGenCppMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = this.getGenCppParameterTypeName(parameterType);
			final String parameterName;
			if(this.isPrimitiveType(parameterType)) {
				parameterName = pParameterNames[i];
			} else {
				parameterName = pParameterNames[i] + this.mProxyCppClassSuffix;
			}

			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(parameterTypeName).append(' ').append(parameterName);
		}

		return stringBuilder.toString();
	}

	public String getGenCppMethodCallParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getGenCppMethodCallParamatersAsString(parameterTypes, parameterNames);
	}

	public String getGenCppMethodCallParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterName;
			if(this.isPrimitiveType(parameterType)) {
				parameterName = pParameterNames[i];
			} else {
				parameterName = this.getGenCppLocalVariableParameterName(pParameterNames[i], true);
			}

			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(parameterName);
		}

		return stringBuilder.toString();
	}

	public String getGenCppLocalVariableParameterName(final String pParameterName) {
		return this.getGenCppLocalVariableParameterName(pParameterName, false);
	}

	public String getGenCppLocalVariableParameterName(final String pParameterName, final boolean pAddressify) {
		return ((pAddressify) ? "&" : "") + this.uncapitalizeFirstCharacter((pParameterName + this.mProxyCppClassSuffix).substring(1));
	}

	public String getJNIMethodCallParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getJNIMethodCallParamatersAsString(parameterTypes, parameterNames);
	}

	public String getJNIMethodCallParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		if(pParameterTypes.length == 0) {
			return null;
		}
		final StringBuilder stringBuilder = new StringBuilder();

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterName;
			if(this.isPrimitiveType(parameterType)) {
				parameterName = pParameterNames[i];
			} else {
				parameterName = pParameterNames[i] + this.mProxyCppClassSuffix + "->unwrap()";
			}

			if(i == 0) {
				stringBuilder.append("");
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(parameterName);
		}

		return stringBuilder.toString();
	}

	public String getJNIExportMethodParamatersAsString(final AccessibleObject pAccessibleObject) throws IllegalArgumentException {
		final Class<?>[] parameterTypes = this.getParameterTypes(pAccessibleObject);
		final String[] parameterNames = this.getParameterNames(pAccessibleObject);

		return this.getJNIExportMethodParamatersAsString(parameterTypes, parameterNames);
	}

	public String getJNIExportMethodParamatersAsString(final Class<?>[] pParameterTypes, final String[] pParameterNames) {
		final StringBuilder stringBuilder = new StringBuilder("JNIEnv* pJNIEnv, jobject pJObject, jlong pAddress");

		for(int i = 0; i < pParameterTypes.length; i++) {
			final Class<?> parameterType = pParameterTypes[i];
			final String parameterTypeName = this.getJNIParameterTypeName(parameterType);
			final String parameterName = pParameterNames[i];

			stringBuilder.append(", ").append(parameterTypeName).append(' ').append(parameterName);
		}

		return stringBuilder.toString();
	}

	public String getGenCppStaticMethodIDFieldName(final Method pMethod) {
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();

		final StringBuilder signatureBuilder = new StringBuilder("sMethod__" + this.capitalizeFirstCharacter(pMethod.getName()));
		if(parameterTypes.length > 0) {
			signatureBuilder.append("__");
			for(final Class<?> parameteType : parameterTypes) {
				final String jniMethodSignatureType = this.getJNIMethodSignatureType(parameteType).replace("L", "__").replace('/', '_').replace(";", "__");
				signatureBuilder.append(jniMethodSignatureType);
			}
		}
		return signatureBuilder.toString();
	}

	public String getGenCppStaticMethodIDFieldName(final Constructor<?> pConstructor) {
		final Class<?>[] parameterTypes = pConstructor.getParameterTypes();
		final StringBuilder signatureBuilder = new StringBuilder("sConstructor");
		if(parameterTypes.length > 0) {
			signatureBuilder.append("__");
			for(final Class<?> parameteType : parameterTypes) {
				final String jniMethodSignatureType = this.getJNIMethodSignatureType(parameteType).replace("L", "__").replace('/', '_').replace(";", "__");
				signatureBuilder.append(jniMethodSignatureType);
			}
		}
		return signatureBuilder.toString();
	}

	public GenCppClassHeaderFileSegment getGenCppClassHeaderFileSegmentByVisibilityModifier(final int modifiers) {
		if(Modifier.isPublic(modifiers)) {
			return GenCppClassHeaderFileSegment.METHODS_PUBLIC;
		} else if(Modifier.isProtected(modifiers)) {
			return GenCppClassHeaderFileSegment.METHODS_PROTECTED;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getJNIParameterTypeName(final Class<?> parameterType) {
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

	public String getJNIMethodSignature(final Constructor<?> pConstructor) {
		final Class<?>[] parameterTypes = pConstructor.getParameterTypes();
		final Class<?>[] expandedParameterTypes = new Class<?>[parameterTypes.length + 1];
		System.arraycopy(parameterTypes, 0, expandedParameterTypes, 1, parameterTypes.length);
		expandedParameterTypes[0] = Long.TYPE;
		return this.getJNIMethodSignature(expandedParameterTypes, Void.TYPE);
	}

	public String getJNIMethodSignature(final Method pMethod) {
		return this.getJNIMethodSignature(pMethod.getParameterTypes(), pMethod.getReturnType());
	}

	public String getJNIMethodSignature(final Class<?>[] pParameterTypes, final Class<?> pReturnType) {
		final StringBuilder signatureBuilder = new StringBuilder();
		signatureBuilder.append("(");
		for(final Class<?> parameteType : pParameterTypes) {
			signatureBuilder.append(this.getJNIMethodSignatureType(parameteType));
		}
		signatureBuilder.append(")");
		signatureBuilder.append(this.getJNIMethodSignatureType(pReturnType));
		return signatureBuilder.toString();
	}

	public String getJNIMethodSignatureType(final Class<?> pType) {
		if(pType.isArray()) {
			return "[" + this.getJNIMethodSignatureType(pType.getComponentType());
		}

		if(pType == Void.TYPE) {
			return "V";
		} else if(pType == Boolean.TYPE) {
			return "Z";
		} else if(pType == Byte.TYPE) {
			return "B";
		} else if(pType == Character.TYPE) {
			return "C";
		} else if(pType == Short.TYPE) {
			return "S";
		} else if(pType == Integer.TYPE) {
			return "I";
		} else if(pType == Long.TYPE) {
			return "J";
		} else if(pType == Float.TYPE) {
			return "F";
		} else if(pType == Double.TYPE) {
			return "D";
		} else {
			return "L" + pType.getName().replace('.', '/') + ";";
		}
	}

	public String getJNICallXYZMethodName(final Class<?> pType) {
		if(pType == Void.TYPE) {
			return "CallVoidMethod";
		} else if(pType == Boolean.TYPE) {
			return "CallBooleanMethod";
		} else if(pType == Byte.TYPE) {
			return "CallByteMethod";
		} else if(pType == Character.TYPE) {
			return "CallCharMethod";
		} else if(pType == Short.TYPE) {
			return "CallShortMethod";
		} else if(pType == Integer.TYPE) {
			return "CallIntMethod";
		} else if(pType == Long.TYPE) {
			return "CallLongMethod";
		} else if(pType == Float.TYPE) {
			return "CallFloatMethod";
		} else if(pType == Double.TYPE) {
			return "CallDoubleMethod";
		} else if(pType == String.class) {
			return "CallStringMethod";
		} else {
			return "CallObjectMethod";
		}
	}

	public String getGenCppParameterTypeName(final Class<?> pParameterType) {
		return this.getGenCppParameterTypeName(pParameterType, true);
	}

	public String getGenCppParameterTypeName(final Class<?> pParameterType, final boolean pPointerObjects) {
		if(pParameterType.isArray()) {
			final Class<?> componentType = pParameterType.getComponentType();
			if(componentType == Boolean.TYPE) {
				return "jbooleanArray";
			} else if(componentType == Byte.TYPE) {
				return "jbyteArray";
			} else if(componentType == Character.TYPE) {
				return "jcharArray";
			} else if(componentType == Short.TYPE) {
				return "jshortArray";
			} else if(componentType == Integer.TYPE) {
				return "jintArray";
			} else if(componentType == Long.TYPE) {
				return "jlongArray";
			} else if(componentType == Float.TYPE) {
				return "jfloatArray";
			} else if(componentType == Double.TYPE) {
				return "jdoubleArray";
			} else if(componentType == String.class) {
				return "jstringArray";
			} else if(componentType == Object.class) {
				return "jobjectArray";
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			if(pParameterType == Void.TYPE) {
				return "void";
			} else if(pParameterType == Boolean.TYPE) {
				return "jboolean";
			} else if(pParameterType == Byte.TYPE) {
				return "jbyte";
			} else if(pParameterType == Character.TYPE) {
				return "jchar";
			} else if(pParameterType == Short.TYPE) {
				return "jshort";
			} else if(pParameterType == Integer.TYPE) {
				return "jint";
			} else if(pParameterType == Long.TYPE) {
				return "jlong";
			} else if(pParameterType == Float.TYPE) {
				return "jfloat";
			} else if(pParameterType == Double.TYPE) {
				return "jdouble";
			} else if(pParameterType == String.class) {
				return "jstring";
			} else if(pParameterType == Object.class) {
				return "jobject";
			} else {
				// TODO Add import, when name != simplename.
				if(pPointerObjects) {
					return pParameterType.getSimpleName() + this.mProxyCppClassSuffix + "*";
				} else {
					return pParameterType.getSimpleName() + this.mProxyCppClassSuffix;
				}
			}
		}
	}

	public boolean isPrimitiveType(final Class<?> pType) {
		return this.isPrimitiveType(pType, true);
	}

	public boolean isPrimitiveType(final Class<?> pType, final boolean pAllowArray) {
		if(pType.isArray()) {
			if(pAllowArray) {
				return this.isPrimitiveType(pType.getComponentType());
			} else {
				return false;
			}
		} else {
			if(pType == Void.TYPE) {
				return true;
			} else if(pType == Boolean.TYPE) {
				return true;
			} else if(pType == Byte.TYPE) {
				return true;
			} else if(pType == Character.TYPE) {
				return true;
			} else if(pType == Short.TYPE) {
				return true;
			} else if(pType == Integer.TYPE) {
				return true;
			} else if(pType == Long.TYPE) {
				return true;
			} else if(pType == Float.TYPE) {
				return true;
			} else if(pType == Double.TYPE) {
				return true;
			} else if(pType == String.class) {
				return true;
			} else if(pType == Object.class) {
				return true;
			} else {
				return false;
			}
		}
	}

	public String[] getParameterNames(final AccessibleObject pAccessibleObject) {
		final BytecodeReadingParanamer bytecodeReadingParanamer = new BytecodeReadingParanamer();
		return bytecodeReadingParanamer.lookupParameterNames(pAccessibleObject);
	}

	public Class<?>[] getParameterTypes(final AccessibleObject pAccessibleObject) {
		if(pAccessibleObject instanceof Constructor<?>) {
			return ((Constructor<?>)pAccessibleObject).getParameterTypes();
		} else if(pAccessibleObject instanceof Method) {
			return ((Method)pAccessibleObject).getParameterTypes();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean isGenMethodIncluded(final Method pMethod) {
		final String methodName = pMethod.getName();
		for(final String genMethodInclude : this.mProxyMethodsInclude) {
			if(genMethodInclude.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGenClassIncluded(final Class<?> pClass) {
		final String className = pClass.getName();
		for(final String genClassExclude : this.mProxyClassesExclude) {
			if(genClassExclude.equals(className)) {
				return false;
			}
		}
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
