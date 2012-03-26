package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassSourceFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter.GenJavaClassSourceFileSegment;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.thoughtworks.paranamer.ParameterNamesNotFoundException;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:48:25 - 20.03.2012
 */
public class Generator {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	@Option(required = true, name = "-in-java-root")
	private File mInJavaRoot;

	@Option(required = true, name = "-in-javabin-root")
	private File mInJavaBinRoot;

	@Option(required = true, name = "-gen-cpp-root")
	private File mGenCppRoot;

	@Option(required = true, name = "-gen-java-root")
	private File mGenJavaRoot;

	@Option(required = false, name = "-gen-java-class-suffix")
	private String mGenJavaClassSuffix = "Proxy";

	@Option(required = false, name = "-gen-java-formatter")
	private JavaFormatter mGenJavaFormatter;

	@Option(required = false, name = "-gen-cpp-class-suffix")
	private String mGenCppClassSuffix;

	@Option(required = false, name = "-gen-cpp-formatter")
	private CppFormatter mGenCppFormatter;

	@Option(required = false, name = "-gen-method-exclude", multiValued = true)
	private List<String> mGenMethodsExclude;

	@Option(required = true, name = "-class", multiValued = true)
	private List<String> mFullyQualifiedClassNames;

	private final File mInJavaBinRootClasses;

	// ===========================================================
	// Constructors
	// ===========================================================

	public static void main(final String[] pArgs) throws Exception {
		new Generator(pArgs);
	}


	public Generator(final String[] pArgs) throws CmdLineException {
		final CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(pArgs);

		this.mInJavaBinRootClasses = new File(this.mInJavaBinRoot, "classes/");

		this.checkArguments();

		this.generateCode();
	}

	private void checkArguments() {
		if(!this.mInJavaRoot.exists()) {
			throw new IllegalArgumentException("TODO Explain!");
		}

		if(!this.mInJavaBinRoot.exists()) {
			throw new IllegalArgumentException("TODO Explain!");
		}

		if(!this.mGenCppRoot.exists()) {
			throw new IllegalArgumentException("TODO Explain!");
		}

		if(!this.mGenJavaRoot.exists()) {
			throw new IllegalArgumentException("TODO Explain!");
		}

		if(this.mGenJavaClassSuffix.contains(" ")) {
			throw new IllegalArgumentException("TODO Explain!");
		}

		for(final String className : this.mFullyQualifiedClassNames) {
			final File classSourceFile = Util.getInJavaClassSourceFile(this.mInJavaRoot, className);
			if(!classSourceFile.exists()) {
				throw new IllegalArgumentException("'" + classSourceFile + "' does not exist!");
			}
			final File classFile = Util.getInJavaClassFile(this.mInJavaBinRootClasses, className);
			if(!classFile.exists()) {
				throw new IllegalArgumentException("'" + classFile + "' does not exist!");
			}
		}
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

	private void generateCode() {
		for(final String className : this.mFullyQualifiedClassNames) {
			try {
				final URI uri = this.mInJavaBinRootClasses.toURI();
				final ClassLoader classLoader = new URLClassLoader(new URL[]{uri.toURL()});

				final Class<?> clazz = classLoader.loadClass(className);

				System.out.print("Generating: '" + className + "' ...");
				this.generateClassCode(clazz);
				System.out.println(" done!");
			} catch (final Throwable t) {
				t.printStackTrace();
				System.out.println(" ERROR!");
			}
		}
	}

	private void generateClassCode(final Class<?> pClass) throws IOException {
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mGenJavaRoot, pClass, this.mGenJavaClassSuffix, this.mGenJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mGenCppClassSuffix, this.mGenCppFormatter);

		genJavaClassFileWriter.begin();
		genCppClassFileWriter.begin();

		this.generateClassHeader(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassFields(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassConstructors(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassFooter(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateClassHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);
		final String genJavaClassPackageName = Util.getGenJavaClassPackageName(pClass);
		final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);

		/* Generate Java boilerplate. */
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
		}

		/* Generate native boilerplate. */
		{
			/* #ifdef. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#ifndef " + genCppClassName + "_H").end();
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#define " + genCppClassName + "_H").end();

			/* Imports. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <jni.h>").end();
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/ScriptingEnvironment.h\"").end();
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end(); // TODO Import 'Superclass.h' instead of Wrapper.h

			/* Externs. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "extern \"C\" {").end();
			pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.EXTERNS);

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
		}

		/* Generate Java header. */
		{
			/* Class. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public class").space().append(genJavaClassName).space().append("extends").space().append(pClass.getSimpleName()).space().append("{").end();

			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.STATIC_METHODS, "public static native void nativeInitClass();").end();
		}

		/* Generate native header. */
		{
			/* Header. */
			{
				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class").space().append(genCppClassName).append(" : ").append("public").space().append("Wrapper").space().append("{").end(); // TODO extend 'Superclass' insteaf of Wra
			}

			/* Source. */
			{
				/* Includes. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, "#include <cstdlib>").end();
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, "#include \"").append(Util.getGenCppClassHeaderImport(pClass, this.mGenCppClassSuffix)).append("\"").end();

				/* Statics. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jclass").space().append(Util.getGenCppStaticClassMemberName(pClass, this.mGenCppClassSuffix)).append(";").end();
			}
		}
	}

	private void generateClassFooter(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		/* Generate Java footer. */
		pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_END, "}").end();

		/* Generate native footer. */
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "};").end();
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "#endif").end();

		/* Externs. */
		pGenCppClassFileWriter.decrementIndent(GenCppClassHeaderFileSegment.EXTERNS);
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "}").end();
	}

	private void generateClassFields(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.FIELDS, "private final long mAddress;").end();
	}

	private void generateClassConstructors(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws ParameterNamesNotFoundException {
		for(final Constructor<?> constructor : pClass.getConstructors()) {
			this.generateClassConstructor(pClass, constructor, pGenJavaClassFileWriter, pGenCppClassFileWriter);
		}
	}

	private void generateClassConstructor(final Class<?> pClass, final Constructor<?> pConstructor, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);
		final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);

		final int modifiers = pConstructor.getModifiers();
		if(!Modifier.isPrivate(modifiers)) {
			final String visibilityModifiers = Util.getVisibilityModifiersAsString(pConstructor);

			/* Generate Java constructors. */
			{
				final String methodParamatersAsString = Util.getJavaMethodParamatersAsString(pConstructor);
				final String methodCallParamatersAsString = Util.getJavaMethodCallParamatersAsString(pConstructor);

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
				this.generateImports(pConstructor, pGenJavaClassFileWriter, pGenCppClassFileWriter);
			}

			/* Generate native constructor. */
			{
				final GenCppClassSourceFileSegment genCppClassSourceFileSegment = Generator.getGenCppClassSourceFileSegmentByVisibilityModifier(modifiers);
				final GenCppClassHeaderFileSegment genCppClassHeaderFileSegment = Generator.getGenCppClassHeaderFileSegmentByVisibilityModifier(modifiers);

				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppClassName);
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, "(");
				final String genCppMethodHeaderParamatersAsString = Util.getGenCppMethodHeaderParamatersAsString(pConstructor);
				if(genCppMethodHeaderParamatersAsString != null) {
					pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppMethodHeaderParamatersAsString);
				}
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, ");").end();
				// Sprite(float, float, float, float, TextureRegion*, VertexBufferObjectManager*);
			}
		}
	}

	private void generateClassMethods(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Method method : pClass.getMethods()) {
			if(!this.isGenMethodExcluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has")) {
					this.generateImports(method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
					this.generateGetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("set")) {
					this.generateImports(method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
					this.generateSetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("on")) {
					this.generateImports(method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
					this.generateCallback(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else {
					System.err.println("Skipping method: " + pClass.getSimpleName() + "." + methodName + "(...) !");
				}
			}
		}
	}

	private void generateCallback(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		final String methodName = pMethod.getName();
		if((returnType == Boolean.TYPE) || (returnType == Void.TYPE)) {
			if(Modifier.isPublic(pMethod.getModifiers())) { // TODO Is this check correct?
				/* Generate Java side of the callback. */
				final String javaNativeMethodName = Util.getJavaNativeMethodName(pMethod);
				final String jniExportMethodName = Util.getJNIExportMethodName(pMethod, this.mGenJavaClassSuffix);
				final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);
				final String uncapitalizedGenCppClassName = Util.uncapitalizeFirstCharacter(genCppClassName);

				{
					/* Source. */
					final String visibilityModifier = Util.getVisibilityModifiersAsString(pMethod);
					final String methodParamatersAsString = Util.getJavaMethodParamatersAsString(pMethod);
					final String methodCallParamatersAsString = Util.getJavaMethodCallParamatersAsString(pMethod);

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
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "this.mAddress"); // TODO Parameters
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ")");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ") {").end();
						pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "super.").append(methodName);
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, "(");
						pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.METHODS, ""); // TODO Parameters
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
					/* Header. */
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT jboolean JNICALL").space().append(jniExportMethodName);
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "(");
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, Util.getJNIExportMethodHeaderParamatersAsString(pMethod));
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, ");").end();

					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "bool").space().append(methodName).append("();").end(); // TODO Parameters

					/* Source. */
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "JNIEXPORT jboolean JNICALL").space().append(jniExportMethodName);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "(");
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, Util.getJNIExportMethodParamatersAsString(pMethod));
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, ") {").end();
					pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, genCppClassName).append("*").space().append(uncapitalizedGenCppClassName).append(" = ").append("(").append(genCppClassName).append("*)").append("pAddress;").end();
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "return").space().append(uncapitalizedGenCppClassName).append("->").append("onAttached").append("();").end(); // TODO Parameters
					pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.JNI_EXPORTS);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.JNI_EXPORTS, "}").end();

					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, "bool").space().append(genCppClassName).append("::").append(methodName);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, "(");
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, "");  // TODO Parameters
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, ");").end();
					pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS_PUBLIC);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, "return false;").end(); // TODO Parameters
					pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS_PUBLIC);
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS_PUBLIC, "}").end(); // TODO Parameters
				}
			} else {
				System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
			}
		} else {
			System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
		}
	}

	private void generateSetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		// TODO Generate code.
	}

	private void generateGetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		// TODO Support all types?
		if((returnType == Byte.TYPE) || (returnType == Short.TYPE) || (returnType == Integer.TYPE) || (returnType == Long.TYPE) || (returnType == Float.TYPE) || (returnType == Double.TYPE) || (returnType == Boolean.TYPE)) {
			// TODO Generate code.
		}
	}

	private boolean isGenMethodExcluded(final Method pMethod) {
		final String methodName = pMethod.getName();
		for(final String genMethodExclude : this.mGenMethodsExclude) {
			if(genMethodExclude.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	private static GenCppClassHeaderFileSegment getGenCppClassHeaderFileSegmentByVisibilityModifier(final int modifiers) {
		if(Modifier.isPublic(modifiers)) {
			return GenCppClassHeaderFileSegment.METHODS_PUBLIC;
		} else if(Modifier.isProtected(modifiers)) {
			return GenCppClassHeaderFileSegment.METHODS_PROTECTED;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static GenCppClassSourceFileSegment getGenCppClassSourceFileSegmentByVisibilityModifier(final int modifiers) {
		if(Modifier.isPublic(modifiers)) {
			return GenCppClassSourceFileSegment.METHODS_PUBLIC;
		} else if(Modifier.isProtected(modifiers)) {
			return GenCppClassSourceFileSegment.METHODS_PROTECTED;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void generateImports(final AccessibleObject pAccessibleObject, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			this.generateImports(((Constructor<?>)pAccessibleObject).getParameterTypes(), pGenJavaClassFileWriter, pGenCppClassFileWriter);
		} else if(pAccessibleObject instanceof Method) {
			this.generateImports(((Method)pAccessibleObject).getParameterTypes(), pGenJavaClassFileWriter, pGenCppClassFileWriter);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void generateImports(final Class<?>[] pParameterTypes, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Class<?> parameterType : pParameterTypes) {
			if(!Util.isPrimitiveParameter(parameterType)) {
				final String parameterTypeName = Util.getClassNameForImport(parameterType);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, "import").space().append(parameterTypeName).append(";").end();
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}