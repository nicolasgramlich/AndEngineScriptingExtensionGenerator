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

	@Option(required = false, name = "-gen-method-include", multiValued = true)
	private List<String> mGenMethodsInclude;

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
				if(clazz.isInterface()) {
					this.generateInterfaceCode(clazz);
				} else if(clazz.isEnum()) {
					this.generateEnumCode(clazz);
				} else {
					this.generateClassCode(clazz);
				}
				System.out.println(" done!");
			} catch (final Throwable t) {
				t.printStackTrace();
				System.out.println(" ERROR!");
			}
		}
	}

	private void generateEnumCode(final Class<?> pClazz) {
		
	}

	private void generateInterfaceCode(final Class<?> pClass) throws IOException {
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mGenCppClassSuffix, this.mGenCppFormatter, true);
		genCppClassFileWriter.begin();

		this.generateInterfaceHeader(pClass, genCppClassFileWriter);
		this.generateInterfaceMethods(pClass, genCppClassFileWriter);
		this.generateInterfaceFooter(pClass, genCppClassFileWriter);

		genCppClassFileWriter.end();
	}
	
	private void generateInterfaceHeader(final Class<?> pClass, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);
		/* Generate native header. */
		{
			/* Header. */
			{
				/* #ifdef. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#ifndef " + genCppClassName + "_H").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#define " + genCppClassName + "_H").end();

				/* Imports. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <jni.h>").end();

				/* Class. */
				final Class<?>[] interfaces = pClass.getInterfaces();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class").space().append(genCppClassName);
				this.generateIncludes(interfaces, pGenCppClassFileWriter);
				if(interfaces.length > 0) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, " : ");

					for(int i = 0; i < interfaces.length; i++) {
						if(i > 0) {
							pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, ", ");
						}
						final Class<?> interfaze = interfaces[i];
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append(Util.getGenCppClassName(interfaze, this.mGenCppClassSuffix));
					}
				}
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, " {").end();

				/* Methods. */
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "public:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
			}
		}
	}

	private void generateInterfaceMethods(final Class<?> pClass, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Method method : pClass.getMethods()) {
			if(!this.isGenMethodIncluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has") || methodName.startsWith("set")) {
					this.generateIncludes(method, pGenCppClassFileWriter);
					this.generateInterfaceMethod(pClass, method, pGenCppClassFileWriter);
				} else if(methodName.startsWith("on")) {
					this.generateIncludes(method.getParameterTypes(), pGenCppClassFileWriter);
					this.generateInterfaceCallback(pClass, method, pGenCppClassFileWriter);
				} else {
//					System.err.println("Skipping interface method: " + pClass.getSimpleName() + "." + methodName + "(...) !");
				}
			}
		}
	}

	private void generateInterfaceMethod(Class<?> pClass, Method pMethod, GenCppClassFileWriter pGenCppClassFileWriter) {
		
	}

	private void generateInterfaceCallback(final Class<?> pClass, final Method pMethod, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppMethodHeaderParamatersAsString = Util.getGenCppMethodHeaderParamatersAsString(pMethod, this.mGenCppClassSuffix);
		final String methodName = pMethod.getName();

		final String returnTypeName;
		if(pMethod.getReturnType() == Void.TYPE) {
			returnTypeName = Util.getGenCppParameterTypeName(Boolean.TYPE, this.mGenCppClassSuffix);
		} else {
			returnTypeName = Util.getGenCppParameterTypeName(pMethod.getReturnType(), this.mGenCppClassSuffix);
		}
		
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual").space().append(returnTypeName).space().append(methodName);
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "(");
		if(genCppMethodHeaderParamatersAsString != null) {
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppMethodHeaderParamatersAsString);
		}
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, ")");
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, " = 0;").end();
	}

	private void generateInterfaceFooter(final Class<?> pClass, final GenCppClassFileWriter pGenCppClassFileWriter) {
		/* Generate native footer. */
		{
			/* Class. */
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "};").end();
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_END, "#endif").end();
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

		if(!Modifier.isAbstract(pClass.getModifiers())) {
			this.generateClassMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		}
		this.generateClassFooter(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateClassHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);
		final String genJavaClassPackageName = Util.getGenJavaClassPackageName(pClass);
		final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);

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
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <jni.h>").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/AndEngineScriptingExtension.h\"").end();

				/* Externs. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "extern \"C\" {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.EXTERNS);
				
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT void JNICALL ").append(Util.getJNIExportMethodName(pClass, "initClass", this.mGenJavaClassSuffix)).append("(JNIEnv*, jclass);").end();
				

				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class").space().append(genCppClassName).append(" : ");
				final Class<?> superclass = pClass.getSuperclass();
				if(Object.class.equals(superclass)) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append("Wrapper");
				} else {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, Util.getGenCppClassInclude(superclass, this.mGenCppClassSuffix)).end();
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append(Util.getGenCppClassName(superclass, this.mGenCppClassSuffix));
				}
				final Class<?>[] interfaces = pClass.getInterfaces();
				this.generateIncludes(interfaces, pGenCppClassFileWriter);
				for(final Class<?> interfaze : interfaces) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, ",").space().append("public").space().append(Util.getGenCppClassName(interfaze, this.mGenCppClassSuffix));
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

				/* Wrapper-Constructor */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppClassName).append("(jobject);").end();
			}

			/* Source. */
			{
				/* Includes. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, "#include <cstdlib>").end();
				final String genCppClassInclude = Util.getGenCppClassInclude(pClass, this.mGenCppClassSuffix);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, genCppClassInclude).end();

				/* Statics. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jclass").space().append(Util.getGenCppStaticClassMemberName(pClass, this.mGenCppClassSuffix)).append(";").end();

				/* Wrapper-Constructor */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("(jobject p").append(genJavaClassName).append(") {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = p").append(genJavaClassName).append(";").end();
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
		final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);
		/* Header. */
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppClassName).append("();").end();

		/* Source. */
		pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("() {").end();
		pGenCppClassFileWriter.endLine(GenCppClassSourceFileSegment.METHODS);
		pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
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
				final GenCppClassSourceFileSegment genCppClassSourceFileSegment = Generator.getGenCppClassSourceFileSegmentByVisibilityModifier(modifiers);
				final GenCppClassHeaderFileSegment genCppClassHeaderFileSegment = Generator.getGenCppClassHeaderFileSegmentByVisibilityModifier(modifiers);

				/* Header. */
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppClassName);
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, "(");
				final String genCppMethodHeaderParamatersAsString = Util.getGenCppMethodHeaderParamatersAsString(pConstructor, this.mGenCppClassSuffix);
				if(genCppMethodHeaderParamatersAsString != null) {
					pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, genCppMethodHeaderParamatersAsString);
				}
				pGenCppClassFileWriter.append(genCppClassHeaderFileSegment, ");").end();

				/* Source. */
				// TODO 
			}
		}
	}

	private void generateClassMethods(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Method method : pClass.getMethods()) {
			if(this.isGenMethodIncluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has")) {
					this.generateClassGetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("set")) {
					this.generateClassSetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("on")) {
					this.generateClassCallback(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else {
//					System.err.println("Skipping class method: " + pClass.getSimpleName() + "." + methodName + "(...) !");
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

				final String[] parameterNames = Util.getParameterNames(pMethod);
				final Class<?>[] parameterTypes = Util.getParameterTypes(pMethod);

				/* Generate Java side of the callback. */
				final String javaNativeMethodName = Util.getJavaNativeMethodName(pMethod);
				final String jniExportMethodName = Util.getJNIExportMethodName(pClass, pMethod, this.mGenJavaClassSuffix);
				final String genCppClassName = Util.getGenCppClassName(pClass, this.mGenCppClassSuffix);
				final String uncapitalizedGenCppClassName = Util.uncapitalizeFirstCharacter(genCppClassName);

				{
					final String visibilityModifier = Util.getVisibilityModifiersAsString(pMethod);
					final String methodParamatersAsString = Util.getJavaMethodParamatersAsString(pMethod);
					final String methodCallParamatersAsString = Util.getJavaMethodCallParamatersAsString(pMethod);

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
					final String jniExportMethodHeaderParamatersAsString = Util.getJNIExportMethodHeaderParamatersAsString(pMethod);
					final String jniExportMethodParamatersAsString = Util.getJNIExportMethodParamatersAsString(pMethod);
					final String cppMethodHeaderParamatersAsString = Util.getGenCppMethodHeaderParamatersAsString(pMethod, this.mGenCppClassSuffix);
					final String cppMethodParamatersAsString = Util.getGenCppMethodParamatersAsString(pMethod, this.mGenCppClassSuffix);
					final String cppMethodCallParamatersAsString = Util.getGenCppMethodCallParamatersAsString(pMethod, this.mGenCppClassSuffix);

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
							this.generateIncludes(parameterTypes, pGenCppClassFileWriter);
							for(int i = 0; i < parameterTypes.length; i++) {
								final Class<?> parameterType = parameterTypes[i];
								final String parameterName = parameterNames[i];
								if(!Util.isPrimitiveType(parameterType)) {
									final String genCppParameterTypeName = Util.getGenCppClassName(parameterType, this.mGenCppClassSuffix);
									final String uncapitalizedGenCppParameterTypeName = Util.getGenCppLocalVariableParameterName(parameterName, this.mGenCppClassSuffix);
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

	private void generateClassSetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		if(false) {
			this.generateParameterImportsAndIncludes(pMethod, pGenJavaClassFileWriter, pGenCppClassFileWriter);
		}
		// TODO Generate code.
	}

	private void generateClassGetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		// TODO Support all types?
		if((returnType == Byte.TYPE) || (returnType == Short.TYPE) || (returnType == Integer.TYPE) || (returnType == Long.TYPE) || (returnType == Float.TYPE) || (returnType == Double.TYPE) || (returnType == Boolean.TYPE)) {
			if(false) {
				this.generateParameterImportsAndIncludes(pMethod, pGenJavaClassFileWriter, pGenCppClassFileWriter);
			}

			// TODO Generate code.
		}
	}

	private boolean isGenMethodIncluded(final Method pMethod) {
		final String methodName = pMethod.getName();
		for(final String genMethodInclude : this.mGenMethodsInclude) {
			if(genMethodInclude.equals(methodName)) {
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
			return GenCppClassSourceFileSegment.METHODS;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void generateParameterImportsAndIncludes(final AccessibleObject pAccessibleObject, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;
			final Class<?>[] parameterTypes = constructor.getParameterTypes();

			this.generateImports(parameterTypes, pGenJavaClassFileWriter);
			this.generateIncludes(constructor.getParameterTypes(), pGenCppClassFileWriter);
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();

			this.generateImports(parameterTypes, pGenJavaClassFileWriter);
			this.generateIncludes(parameterTypes, pGenCppClassFileWriter);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void generateIncludes(final AccessibleObject pAccessibleObject, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;
			
			this.generateIncludes(constructor.getParameterTypes(), pGenCppClassFileWriter);
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();
			
			this.generateIncludes(parameterTypes, pGenCppClassFileWriter);
		} else {
			throw new IllegalArgumentException();
		}
	}

	private void generateImports(final Class<?>[] pTypes, final GenJavaClassFileWriter pGenJavaClassFileWriter) {
		for(final Class<?> type : pTypes) {
			if(!Util.isPrimitiveType(type)) {
				final String genJavaImportClassName = Util.getGenJavaClassImport(type);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, genJavaImportClassName).end();
			}
		}
	}

	private void generateIncludes(final Class<?>[] pTypes, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Class<?> type : pTypes) {
			if(!Util.isPrimitiveType(type)) {
				final String genCppIncludeClassName = Util.getGenCppClassInclude(type, this.mGenCppClassSuffix);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, genCppIncludeClassName).end();
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}