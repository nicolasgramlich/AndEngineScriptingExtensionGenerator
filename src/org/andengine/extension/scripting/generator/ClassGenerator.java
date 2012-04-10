package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassSourceFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter.GenJavaClassSourceFileSegment;

import com.thoughtworks.paranamer.ParameterNamesNotFoundException;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:48:25 - 20.03.2012
 */
public class ClassGenerator extends Generator {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final File mProxyCppRoot;
	private final File mProxyJavaRoot;
	private final JavaFormatter mGenJavaFormatter;
	private final CppFormatter mGenCppFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ClassGenerator(final File pProxyJavaRoot, final File pProxyCppRoot, final JavaFormatter pGenJavaFormatter, final CppFormatter pGenCppFormatter, final Util pUtil) {
		super(pUtil);

		this.mProxyCppRoot = pProxyCppRoot;
		this.mProxyJavaRoot = pProxyJavaRoot;
		this.mGenJavaFormatter = pGenJavaFormatter;
		this.mGenCppFormatter = pGenCppFormatter;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void generateClassCode(final Class<?> pClass) throws IOException {
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mProxyJavaRoot, pClass, this.mUtil, this.mGenJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mProxyCppRoot, pClass, this.mUtil, this.mGenCppFormatter);

		genJavaClassFileWriter.begin();
		genCppClassFileWriter.begin();

		this.generateClassHeader(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassFields(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		this.generateClassConstructors(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		if(!Modifier.isAbstract(pClass.getModifiers())) {
			this.generateClassMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		}
		this.generateClassFooter(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateClassHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = this.mUtil.getGenJavaClassName(pClass);
		final String genJavaClassPackageName = this.mUtil.getGenJavaClassPackageName(pClass);
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
		final String genCppNativeInitClassJNIExportMethodName = this.mUtil.getJNIExportMethodName(pClass, "initClass");

		/* Generate Java header. */
		{
			/* Package. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.PACKAGE, "package %s;", genJavaClassPackageName).end();

			/* Imports. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, "import %s;", pClass.getName()).end();

			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTANTS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.FIELDS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.GETTERS_SETTERS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.STATIC_METHODS);

			/* Class. */
			if(Modifier.isAbstract(pClass.getModifiers())) {
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public abstract class %s extends %s {", genJavaClassName, pClass.getSimpleName()).end();
			} else {
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public class %s extends %s{", genJavaClassName, pClass.getSimpleName()).end();
			}

			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.STATIC_METHODS, "public static native void nativeInitClass();").end();
		}

		/* Generate native header. */
		{
			/* Header. */
			{
				/* #ifdef. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#ifndef " + genCppClassName + "_H").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#define " + genCppClassName + "_H").end();

				/* Imports. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <memory>").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <jni.h>").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/AndEngineScriptingExtension.h\"").end();

				/* Externs. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "extern \"C\" {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.EXTERNS);

				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT void JNICALL %s(JNIEnv*, jclass);", genCppNativeInitClassJNIExportMethodName).end();

				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class %s : ", genCppClassName);
				final Class<?> superclass = pClass.getSuperclass();
				if(Object.class.equals(superclass)) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public Wrapper");
				} else {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, this.mUtil.getGenCppClassInclude(superclass)).end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public %s", this.mUtil.getGenCppClassName(superclass));
				}
				final Class<?>[] interfaces = pClass.getInterfaces();
				for(final Class<?> interfaze : interfaces) {
					if(this.mUtil.isGenClassIncluded(interfaze)) {
						this.generateIncludes(pGenCppClassFileWriter, interfaze);
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, ", public %s", this.mUtil.getGenCppClassName(interfaze));
					}
				}
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, " {").end();

				/* Methods. */
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "public:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);

				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PROTECTED);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PROTECTED, "protected:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PROTECTED);

				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PRIVATE);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PRIVATE, "private:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PRIVATE);

				/* Wrapper-Constructor. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "%s(jobject);", genCppClassName).end();

				/* Unwrapper. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual jobject unwrap();").end();
			}

			/* Source. */
			{
				/* Includes. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, "#include <cstdlib>").end();
				final String genCppClassInclude = this.mUtil.getGenCppClassInclude(pClass);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, genCppClassInclude).end();

				/* Statics. */
				final String genCppStaticClassMemberName = this.mUtil.getGenCppStaticClassMemberName(pClass);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jclass %s;", genCppStaticClassMemberName).end();

				/* Class init. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "JNIEXPORT void JNICALL %s(JNIEnv* pJNIEnv, jclass pJClass) {", genCppNativeInitClassJNIExportMethodName).end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.CLASS_INIT);

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "%s = (jclass)JNI_ENV()->NewGlobalRef(pJClass);", genCppStaticClassMemberName).end();

				/* Wrapper-Constructor. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "%s::%s(jobject p%s) {", genCppClassName, genCppClassName, genJavaClassName).end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = p%s;", genJavaClassName).end();
				pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();

				/* Unwrapper. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "jobject %s::unwrap() {", genCppClassName).end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "return this->mUnwrapped;").end();
				pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
			}
		}
	}

	private void generateClassFooter(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		/* Generate Java footer. */
		{
			/* Class. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_END, "}").end();
		}

		/* Generate native footer. */
		{
			/* Externs. */
			pGenCppClassFileWriter.decrementIndent(GenCppClassHeaderFileSegment.EXTERNS);
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "}").end();

			/* Class init. */
			pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.CLASS_INIT);
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "}").end();

			/* Class. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "};").end();
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "#endif").end();
		}
	}

	private void generateClassFields(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.FIELDS, "private final long mAddress;").end();
	}

	private void generateClassConstructors(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws ParameterNamesNotFoundException {
		boolean zeroArgumentConstructorFound = false;
		final Constructor<?>[] constructors = pClass.getConstructors();
		for(final Constructor<?> constructor : constructors) {
			if(constructor.getParameterTypes().length == 0) {
				zeroArgumentConstructorFound = true;
			}
			this.generateClassConstructor(pClass, constructor, pGenJavaClassFileWriter, pGenCppClassFileWriter);
		}

		/* We need to generate a zero-arg constructor on the native side, so that the subclasses can make use of this constructor. */
		// TODO Think if generating a protected zero-arg constructor is viable in all cases.
		if(!zeroArgumentConstructorFound) {
			this.generateZeroArgumentNativeConstructor(pClass, pGenJavaClassFileWriter, pGenCppClassFileWriter);
		}
	}

	private void generateZeroArgumentNativeConstructor(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
		/* Header. */
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppClassName).append("();").end();

		/* Source. */
		pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "%s::%s() {", genCppClassName, genCppClassName).end();
		pGenCppClassFileWriter.endLine(GenCppClassSourceFileSegment.METHODS);
		pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
	}

	private void generateClassConstructor(final Class<?> pClass, final Constructor<?> pConstructor, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = this.mUtil.getGenJavaClassName(pClass);
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);

		final int modifiers = pConstructor.getModifiers();
		if(!Modifier.isPrivate(modifiers)) {
			final String visibilityModifiers = this.mUtil.getVisibilityModifiersAsString(pConstructor);

			/* Generate Java constructors. */
			{
				final String methodParamatersAsString = this.mUtil.getJavaMethodParamatersAsString(pConstructor);
				final String methodCallParamatersAsString = this.mUtil.getJavaMethodCallParamatersAsString(pConstructor);

				if(pConstructor.isAnnotationPresent(Deprecated.class)) {
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "@Deprecated").end();
				}
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "%s %s(", visibilityModifiers, genJavaClassName);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "final long pAddress");
				if(methodParamatersAsString != null) {
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ", %s", methodParamatersAsString);
				}
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ") ");
				final Class<?>[] exceptions = pConstructor.getExceptionTypes();
				if(exceptions.length > 0) {
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "throws ");
					for(int i = 0; i < exceptions.length; i++) {
						final Class<?> exception = exceptions[i];
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, exception.getSimpleName());
						this.generateImports(pGenJavaClassFileWriter, exception);
						final boolean isLastException = (i == (exceptions.length - 1));
						if(!isLastException) {
							pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ", ");
						}
					}
				}
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "{").end();
				pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
				/* Super call. */
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "super(");
				if(methodCallParamatersAsString != null) {
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, methodCallParamatersAsString);
				}
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ");").end();

				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "this.mAddress = pAddress;").end();
				pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "}").end();

				/* Add imports. */
				this.generateParameterImportsAndIncludes(pConstructor, pGenJavaClassFileWriter, pGenCppClassFileWriter);
			}

			/* Generate native constructor. */
			{
				final GenCppClassHeaderFileSegment genCppClassHeaderFileSegment = this.mUtil.getGenCppClassHeaderFileSegmentByVisibilityModifier(modifiers);

				final String genCppMethodHeaderParamatersAsString = this.mUtil.getGenCppMethodHeaderParamatersAsString(pConstructor);
				final String genCppMethodParamatersAsString = this.mUtil.getGenCppMethodParamatersAsString(pConstructor);
				final String genJNIMethodCallParamatersAsString = this.mUtil.getJNIMethodCallParamatersAsString(pConstructor);
				final String genCppStaticClassMemberName = this.mUtil.getGenCppStaticClassMemberName(pClass);
				final String jniMethodSignature = this.mUtil.getJNIMethodSignature(pConstructor);

				/* Header. */
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppClassName);
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, "(");
				if(genCppMethodHeaderParamatersAsString != null) {
					pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppMethodHeaderParamatersAsString);
				}
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, ");").end();

				final String constructorName = this.mUtil.getGenCppStaticMethodIDFieldName(pConstructor);
				/* Source. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jmethodID %s;", constructorName).end();

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "%s = JNI_ENV()->GetMethodID(%s, \"<init>\", \"%s\");", constructorName, genCppStaticClassMemberName, jniMethodSignature).end();

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "%s::%s(", genCppClassName, genCppClassName);
				if(genCppMethodParamatersAsString != null) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppMethodParamatersAsString);
				}
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ") {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = JNI_ENV()->NewObject(%s, %s", genCppStaticClassMemberName, constructorName);
				if(genJNIMethodCallParamatersAsString != null) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ", (jlong)this, %s);", genJNIMethodCallParamatersAsString).end();
				} else {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ", (jlong)this);").end();
				}
				pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
			}
		}
	}

	private void generateClassMethods(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Method method : pClass.getMethods()) {
			if(this.mUtil.isGenMethodIncluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("on")) {
					this.generateClassCallback(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else {
					this.generateClassMethod(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				}
			}
		}
	}

	private void generateClassCallback(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		final String methodName = pMethod.getName();
		if((returnType == Boolean.TYPE) || (returnType == Void.TYPE)) {
			if(Modifier.isPublic(pMethod.getModifiers())) { // TODO Is this check correct?
				this.generateParameterImportsAndIncludes(pMethod, pGenJavaClassFileWriter, pGenCppClassFileWriter);

				final String[] parameterNames = this.mUtil.getParameterNames(pMethod);
				final Class<?>[] parameterTypes = this.mUtil.getParameterTypes(pMethod);

				/* Generate Java side of the callback. */
				final String javaNativeMethodName = this.mUtil.getJavaNativeMethodName(pMethod);
				final String jniExportMethodName = this.mUtil.getJNIExportMethodName(pClass, pMethod);
				final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
				final String uncapitalizedGenCppClassName = this.mUtil.uncapitalizeFirstCharacter(genCppClassName);

				{
					final String visibilityModifier = this.mUtil.getVisibilityModifiersAsString(pMethod);
					final String methodParamatersAsString = this.mUtil.getJavaMethodParamatersAsString(pMethod);
					final String methodCallParamatersAsString = this.mUtil.getJavaMethodCallParamatersAsString(pMethod);

					/* Source. */
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "@Override").end();
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "%s %s %s(%s) {", visibilityModifier, returnType.getSimpleName(), methodName, (methodParamatersAsString != null) ? methodParamatersAsString : "").end();

					pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
					if(returnType == Void.TYPE) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "if(!this.%s(this.mAddress%s)) {", javaNativeMethodName, (methodCallParamatersAsString != null) ? ", " + methodCallParamatersAsString : "");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "\tsuper.%s(%s);", methodName, (methodCallParamatersAsString != null) ? methodCallParamatersAsString : "");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();
					} else if(returnType == Boolean.TYPE) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "final boolean handledNative = this.%s(this.mAddress%s);", javaNativeMethodName, (methodCallParamatersAsString != null) ?  ", " + methodCallParamatersAsString : "");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "if(handledNative) {").end();
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "\treturn true;").end();
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "} else {").end();
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "\treturn super.%s(%s);", methodName, (methodParamatersAsString != null) ? methodCallParamatersAsString : "");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();
					} else {
						throw new IllegalStateException("Unexpected return type: '" + returnType.getName() + "'.");
					}
					pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.METHODS);
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();

					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "private native boolean %s(final long pAddress%s);", javaNativeMethodName, (methodParamatersAsString != null) ? ", " + methodParamatersAsString : "");
				}

				/* Generate native side of the callback. */
				{
					final String jniExportMethodHeaderParamatersAsString = this.mUtil.getJNIExportMethodHeaderParamatersAsString(pMethod);
					final String jniExportMethodParamatersAsString = this.mUtil.getJNIExportMethodParamatersAsString(pMethod);
					final String cppMethodHeaderParamatersAsString = this.mUtil.getGenCppMethodHeaderParamatersAsString(pMethod);
					final String cppMethodParamatersAsString = this.mUtil.getGenCppMethodParamatersAsString(pMethod);
					final String cppMethodCallParamatersAsString = this.mUtil.getGenCppMethodCallParamatersAsString(pMethod);

					/* Header. */
					{
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT jboolean JNICALL %s(%s);", jniExportMethodName, jniExportMethodHeaderParamatersAsString).end();

						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual jboolean %s(%s);", methodName, (cppMethodHeaderParamatersAsString != null) ? cppMethodHeaderParamatersAsString : "").end();
					}

					/* Source. */
					{
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "JNIEXPORT jboolean JNICALL %s(%s) {", jniExportMethodName, jniExportMethodParamatersAsString);
						pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "%s* %s = (%s*)pAddress;", genCppClassName, uncapitalizedGenCppClassName, genCppClassName).end();

						/* Wrap non-primitives in local variables on the stack. */
						{
							this.generateIncludes(pGenCppClassFileWriter, parameterTypes);
							for(int i = 0; i < parameterTypes.length; i++) {
								final Class<?> parameterType = parameterTypes[i];
								final String parameterName = parameterNames[i];
								if(!this.mUtil.isPrimitiveType(parameterType)) {
									final String genCppParameterTypeName = this.mUtil.getGenCppClassName(parameterType);
									final String uncapitalizedGenCppParameterTypeName = this.mUtil.getGenCppLocalVariableParameterName(parameterName);
									pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "%s %s(%s);", genCppParameterTypeName, uncapitalizedGenCppParameterTypeName, parameterName).end();
								}
							}
						}

						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "return %s->%s(%s);", uncapitalizedGenCppClassName, methodName, (cppMethodCallParamatersAsString != null) ? cppMethodCallParamatersAsString : "");

						pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "}").end();

						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "jboolean %s::%s(%s) {", genCppClassName, methodName, (cppMethodParamatersAsString != null) ? cppMethodParamatersAsString : "").end();
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "\treturn false;").end();
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
					}
				}
			} else {
				System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
			}
		} else {
			System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
		}
	}

	private void generateClassMethod(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		this.generateParameterImportsAndIncludes(pMethod, pGenJavaClassFileWriter, pGenCppClassFileWriter);

		final String genCppMethodHeaderParamatersAsString = this.mUtil.getGenCppMethodHeaderParamatersAsString(pMethod);
		final String genCppMethodParamatersAsString = this.mUtil.getGenCppMethodParamatersAsString(pMethod);
		final String jniMethodCallParamatersAsString = this.mUtil.getJNIMethodCallParamatersAsString(pMethod);
		final String genCppStaticClassMemberName = this.mUtil.getGenCppStaticClassMemberName(pClass);
		final String genCppStaticMethodIDFieldName = this.mUtil.getGenCppStaticMethodIDFieldName(pMethod);
		final String jniMethodSignature = this.mUtil.getJNIMethodSignature(pMethod);
		final String returnTypeGenCppParameterTypeName = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType(), true);
		final String returnTypeGenCppParameterTypeNameWithoutPtr = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType(), false);
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
		final String methodName = pMethod.getName();

		/* Generate native side of the getter. */
		{
			/* Generate virtual method in Header. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual %s %s(%s);", returnTypeGenCppParameterTypeName, methodName, (genCppMethodHeaderParamatersAsString != null) ? genCppMethodHeaderParamatersAsString : "").end(); // TODO Visiblity Modifier?

			/* Generate static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jmethodID %s;", genCppStaticMethodIDFieldName).end();

			/* Cache static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "%s = JNI_ENV()->GetMethodID(%s, \"%s\", \"%s\");", genCppStaticMethodIDFieldName, genCppStaticClassMemberName, methodName, jniMethodSignature).end();

			/* Call java method using static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "%s %s::%s(%s) {", returnTypeGenCppParameterTypeName, genCppClassName, methodName, (genCppMethodParamatersAsString != null) ? genCppMethodParamatersAsString : "").end();

			final boolean primitiveReturnType = this.mUtil.isPrimitiveType(returnType, false);
			if(primitiveReturnType) {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "\t");
				if(returnType != Void.TYPE) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "return ");
				}
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "JNI_ENV()->%s(this->mUnwrapped, %s%s);", this.mUtil.getJNICallXYZMethodName(pMethod.getReturnType()), genCppStaticMethodIDFieldName, (jniMethodCallParamatersAsString != null) ? ", " + jniMethodCallParamatersAsString: "").end();
			} else {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "\treturn new %s(JNI_ENV()->%s(this->mUnwrapped, %s%s));", returnTypeGenCppParameterTypeNameWithoutPtr, this.mUtil.getJNICallXYZMethodName(pMethod.getReturnType()), genCppStaticMethodIDFieldName, (jniMethodCallParamatersAsString != null) ? ", " + jniMethodCallParamatersAsString: "").end();
			}
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}