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

	private final File mGenCppRoot;
	private final File mGenJavaRoot;
	private final JavaFormatter mGenJavaFormatter;
	private final CppFormatter mGenCppFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ClassGenerator(final File pGenJavaRoot, final File pGenCppRoot, final JavaFormatter pGenJavaFormatter, final CppFormatter pGenCppFormatter, final Util pUtil) {
		super(pUtil);

		this.mGenCppRoot = pGenCppRoot;
		this.mGenJavaRoot = pGenJavaRoot;
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
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mGenJavaRoot, pClass, this.mUtil, this.mGenJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mUtil, this.mGenCppFormatter);

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
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.PACKAGE, "package").space().append(genJavaClassPackageName).append(";").end();

			/* Imports. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, "import").space().append(pClass.getName()).append(";").end();

			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTANTS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.FIELDS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.GETTERS_SETTERS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.STATIC_METHODS);

			/* Class. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public").space();
			if(Modifier.isAbstract(pClass.getModifiers())) {
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "abstract").space().append("class").space().append(genJavaClassName).space().append("extends").space().append(pClass.getSimpleName()).space().append("{").end();
			} else {
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "class").space().append(genJavaClassName).space().append("extends").space().append(pClass.getSimpleName()).space().append("{").end();
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

				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT void JNICALL ").append(genCppNativeInitClassJNIExportMethodName).append("(JNIEnv*, jclass);").end();

				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class").space().append(genCppClassName).append(" : ");
				final Class<?> superclass = pClass.getSuperclass();
				if(Object.class.equals(superclass)) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append("Wrapper");
				} else {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, this.mUtil.getGenCppClassInclude(superclass)).end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append(this.mUtil.getGenCppClassName(superclass));
				}
				final Class<?>[] interfaces = pClass.getInterfaces();
				for(final Class<?> interfaze : interfaces) {
					if(this.mUtil.isGenClassIncluded(interfaze)) {
						this.generateIncludes(pGenCppClassFileWriter, interfaze);
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, ",").space().append("public").space().append(this.mUtil.getGenCppClassName(interfaze));
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
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppClassName).append("(jobject);").end();

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
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jclass").space().append(genCppStaticClassMemberName).append(";").end();

				/* Class init. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "JNIEXPORT void JNICALL").space().append(genCppNativeInitClassJNIExportMethodName).append("(JNIEnv* pJNIEnv, jclass pJClass) {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.CLASS_INIT);

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, genCppStaticClassMemberName).append(" = (jclass)JNI_ENV()->NewGlobalRef(pJClass);").end();

				/* Wrapper-Constructor. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("(jobject p").append(genJavaClassName).append(") {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = p").append(genJavaClassName).append(";").end();
				pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();

				/* Unwrapper. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "jobject ").append(genCppClassName).append("::unwrap() {").end();
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
		pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("() {").end();
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
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, visibilityModifiers).space().append(genJavaClassName).append("(");
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, "final long pAddress");
				if(methodParamatersAsString != null) {
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ", ");
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, methodParamatersAsString);
				}
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CONSTRUCTORS, ") {").end();
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
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jmethodID ").append(constructorName).append(";").end();

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, constructorName).append(" = JNI_ENV()->GetMethodID(").append(genCppStaticClassMemberName).append(", \"<init>\", \"").append(jniMethodSignature).append("\");").end();

				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("(");
				if(genCppMethodParamatersAsString != null) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppMethodParamatersAsString);
				}
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ") {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = JNI_ENV()->NewObject(").append(genCppStaticClassMemberName).append(", ").append(constructorName);
				if(genJNIMethodCallParamatersAsString != null) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ", (jlong)this, ").append(genJNIMethodCallParamatersAsString).append(");").end();
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
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, visibilityModifier).space().append(returnType.getSimpleName()).space().append(methodName).append("(");
					if(methodParamatersAsString != null) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodParamatersAsString);
					}
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ") {").end();

					pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
					if(returnType == Void.TYPE) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "if(!this.").append(javaNativeMethodName);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "(");
						/* Parameters. */
						{
							pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "this.mAddress");
							if(methodCallParamatersAsString != null) {
								pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ", ");
								pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodCallParamatersAsString);
							}
						}
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ")");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ") {").end();
						pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "super.").append(methodName);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "(");
						/* Parameters. */
						{
							if(methodCallParamatersAsString != null) {
								pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodCallParamatersAsString);
							}
						}
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ");").end();
						pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();
					} else if(returnType == Boolean.TYPE) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "final boolean handledNative = ");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "this.").append(javaNativeMethodName).append("(");
						/* Parameters. */
						{
							pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "this.mAddress");
							if(methodCallParamatersAsString != null) {
								pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ", ");
								pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodCallParamatersAsString);
							}
						}
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ");").end();
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "if(handledNative) {").end();
						pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "return true;").end();
						pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "} else {").end();
						pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "return").space().append("super." + methodName).append("(");
						if(methodParamatersAsString != null) {
							pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodCallParamatersAsString);
						}
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ");").end();
						pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();
					} else {
						throw new IllegalStateException("Unexpected return type: '" + returnType.getName() + "'.");
					}
					pGenJavaClassFileWriter.decrementIndent(GenJavaClassSourceFileSegment.METHODS);
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "}").end();

					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "private native boolean");
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, " ");
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, javaNativeMethodName).append("(");
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "final long pAddress");
					if(methodParamatersAsString != null) {
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ", ");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, methodParamatersAsString);
					}
					pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ");");
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
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT jboolean JNICALL").space().append(jniExportMethodName);
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "(");
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, jniExportMethodHeaderParamatersAsString);
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, ");").end();

						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual").space().append("jboolean").space().append(methodName);
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "(");
						if(cppMethodHeaderParamatersAsString != null) {
							pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, cppMethodHeaderParamatersAsString);
						}
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, ");").end();
					}

					/* Source. */
					{
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "JNIEXPORT jboolean JNICALL").space().append(jniExportMethodName);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "(");
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, jniExportMethodParamatersAsString);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, ") {").end();
						pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, genCppClassName).append("*").space().append(uncapitalizedGenCppClassName).append(" = ").append("(").append(genCppClassName).append("*)").append("pAddress;").end();

						/* Wrap non-primitives in local variables on the stack. */
						{
							this.generateIncludes(pGenCppClassFileWriter, parameterTypes);
							for(int i = 0; i < parameterTypes.length; i++) {
								final Class<?> parameterType = parameterTypes[i];
								final String parameterName = parameterNames[i];
								if(!this.mUtil.isPrimitiveType(parameterType)) {
									final String genCppParameterTypeName = this.mUtil.getGenCppClassName(parameterType);
									final String uncapitalizedGenCppParameterTypeName = this.mUtil.getGenCppLocalVariableParameterName(parameterName);
									pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, genCppParameterTypeName).space().append(uncapitalizedGenCppParameterTypeName).append("(").append(parameterName).append(");").end();
								}
							}
						}

						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "return").space().append(uncapitalizedGenCppClassName).append("->").append(methodName);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "(");
						if(cppMethodCallParamatersAsString != null) {
							pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, cppMethodCallParamatersAsString);
						}
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, ");").end();

						pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "}").end();

						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "jboolean").space().append(genCppClassName).append("::").append(methodName);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "(");
						if(cppMethodParamatersAsString != null) {
							pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, cppMethodParamatersAsString);
						}
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ") {").end();
						pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
						pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "return false;").end();
						pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
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
		final String returnTypeGenCppParameterTypeName = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType(), true, true);
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
		final String methodName = pMethod.getName();

		/* Generate native side of the getter. */
		{
			/* Generate virutal method in Header. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual").space().append(returnTypeGenCppParameterTypeName).space().append(methodName).append("("); // TODO Visiblity Modifier?
			if(genCppMethodHeaderParamatersAsString != null) {
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppMethodHeaderParamatersAsString);
			}
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, ");").end();

			/* Generate static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static").space().append("jmethodID").space().append(genCppStaticMethodIDFieldName).append(";").end();

			/* Cache static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, genCppStaticMethodIDFieldName).append(" = JNI_ENV()->GetMethodID(").append(genCppStaticClassMemberName).append(", \"").append(methodName).append("\", \"").append(jniMethodSignature).append("\");").end();

			/* Call java method using static methodID field. */
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, returnTypeGenCppParameterTypeName).space().append(genCppClassName).append("::").append(methodName).append("(");
			if(genCppMethodParamatersAsString != null) {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppMethodParamatersAsString);
			}
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ") {").end();
			pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);

			final boolean primitiveReturnType = this.mUtil.isPrimitiveType(returnType, false);
			if(primitiveReturnType) {
				if(returnType != Void.TYPE) {
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "return ");
				}
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "JNI_ENV()->").append(this.mUtil.getJNICallXYZMethodName(pMethod.getReturnType())).append("(this->mUnwrapped, ").append(genCppStaticMethodIDFieldName);
			} else {
				final String returnTypeGenCppParameterTypeNameWithoutAutoPtr = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType(), false, false);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "return ").append(returnTypeGenCppParameterTypeName).append("(new ").append(returnTypeGenCppParameterTypeNameWithoutAutoPtr).append("(JNI_ENV()->").append(this.mUtil.getJNICallXYZMethodName(pMethod.getReturnType())).append("(this->mUnwrapped, ").append(genCppStaticMethodIDFieldName);
			}
			if(jniMethodCallParamatersAsString != null) {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ", ");
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, jniMethodCallParamatersAsString);
			}
			if(primitiveReturnType) {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ");").end();
			} else {
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, ")));").end();
			}
			pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
			pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end(); // TODO Parameters?
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}