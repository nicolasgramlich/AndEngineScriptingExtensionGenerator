package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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

	@Option(required = false, name = "-gen-cpp-formatter")
	private CppFormatter mGenCppFormatter;

	@Option(required = false, name = "-gen-method-exclude", multiValued = true)
	private List<String> mGenMethodsExclude = new ArrayList<String>();

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

				generateClassCode(clazz);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void generateClassCode(final Class<?> pClass) throws IOException {
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mGenJavaRoot, pClass, this.mGenJavaClassSuffix, this.mGenJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mGenCppFormatter);

		genJavaClassFileWriter.begin();
		genCppClassFileWriter.begin();

		generateClassHeader(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		generateClassFields(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		generateClassConstructors(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		generateClassMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.appendSourceLine("}");

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateClassHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws IOException {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);
		final String genJavaClassPackageName = Util.getGenJavaClassPackageName(pClass);

		/* Package. */
		pGenJavaClassFileWriter.appendSource("package");
		pGenJavaClassFileWriter.appendSource(" ");
		pGenJavaClassFileWriter.appendSource(genJavaClassPackageName);
		pGenJavaClassFileWriter.appendSourceLine(";");

		/* Imports. */
		pGenJavaClassFileWriter.appendSource("import");
		pGenJavaClassFileWriter.appendSource(" ");
		pGenJavaClassFileWriter.appendSource(pClass.getName());
		pGenJavaClassFileWriter.appendSourceLine(";");

		/* Class. */
		pGenJavaClassFileWriter.appendSource("public class");
		pGenJavaClassFileWriter.appendSource(" ");
		pGenJavaClassFileWriter.appendSource(genJavaClassName);
		pGenJavaClassFileWriter.appendSource(" ");
		pGenJavaClassFileWriter.appendSource("extends");
		pGenJavaClassFileWriter.appendSource(" ");
		pGenJavaClassFileWriter.appendSource(pClass.getSimpleName());
		pGenJavaClassFileWriter.appendSourceLine(" {");
		
		pGenJavaClassFileWriter.appendSourceLine("public static native void nativeInitClass();");
	}

	private void generateClassFields(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws IOException {
		pGenJavaClassFileWriter.appendSourceLine("private final long mAddress;");
	}

	private void generateClassConstructors(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws IOException {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);

		for(final Constructor<?> constructor : pClass.getConstructors()) {
			if(!Modifier.isPrivate(constructor.getModifiers())) {
				pGenJavaClassFileWriter.appendSource(Util.getConstructorModifiersAsString(constructor));
				pGenJavaClassFileWriter.appendSource(" ");
				pGenJavaClassFileWriter.appendSource(genJavaClassName);
				pGenJavaClassFileWriter.appendSourceLine("(final long pAddress) {"); // TODO Parameters
				pGenJavaClassFileWriter.appendSourceLine("\tsuper();"); // TODO Parameters
				pGenJavaClassFileWriter.appendSourceLine("");
				pGenJavaClassFileWriter.appendSourceLine("\tthis.mAddress = pAddress;");

				pGenJavaClassFileWriter.appendSourceLine("}");
			}
		}
	}

	private void generateClassMethods(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws IOException {
		for(final Method method : pClass.getMethods()) {
			if(!this.isGenMethodExcluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("get")) {
					this.generateGetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("set")) {
					this.generateSetter(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else if(methodName.startsWith("on")) {
					this.generateCallback(pClass, method, pGenJavaClassFileWriter, pGenCppClassFileWriter);
				} else {
//					System.err.println("Skipping method: " + pClass.getSimpleName() + "." + methodName + "(...) !");
				}
			}
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

	private void generateCallback(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) throws IOException {
		final Class<?> returnType = pMethod.getReturnType();

		final String methodName = pMethod.getName();
		if((returnType == Boolean.TYPE) || (returnType == Void.TYPE)) {
			if(Modifier.isPublic(pMethod.getModifiers())) { // TODO Is this check correct?
				final String visibilityModifier = Util.getMethodModifiersAsString(pMethod);
				final String callbackNativeMethodName = Util.generateCallbackNativeMethodName(pMethod);

				pGenJavaClassFileWriter.appendSourceLine("@Override");
				pGenJavaClassFileWriter.appendSource(visibilityModifier);
				pGenJavaClassFileWriter.appendSource(" ");
				pGenJavaClassFileWriter.appendSource(returnType.getSimpleName());
				pGenJavaClassFileWriter.appendSource(" ");
				pGenJavaClassFileWriter.appendSource(methodName);
				pGenJavaClassFileWriter.appendSourceLine("() {"); // TODO Parameters

				if(returnType == Void.TYPE) {
					pGenJavaClassFileWriter.appendSource("\tif(!this.");
					pGenJavaClassFileWriter.appendSource(callbackNativeMethodName);
					pGenJavaClassFileWriter.appendSourceLine("(this.mAddress)) {"); // TODO Parameters
					pGenJavaClassFileWriter.appendSource("\t\tsuper.");
					pGenJavaClassFileWriter.appendSource(methodName);
					pGenJavaClassFileWriter.appendSourceLine("();"); // TODO Parameters
					pGenJavaClassFileWriter.appendSourceLine("\t}");
					pGenJavaClassFileWriter.appendSourceLine("}");
				} else {
					pGenJavaClassFileWriter.appendSource("\tif(!this.");
					pGenJavaClassFileWriter.appendSource(callbackNativeMethodName);
					pGenJavaClassFileWriter.appendSourceLine("(this.mAddress)) {"); // TODO Parameters
					pGenJavaClassFileWriter.appendSourceLine("\t\treturn true;");
					pGenJavaClassFileWriter.appendSourceLine(methodName);
					pGenJavaClassFileWriter.appendSourceLine("();"); // TODO Parameters
					pGenJavaClassFileWriter.appendSourceLine("\t}");
					pGenJavaClassFileWriter.appendSourceLine("}");
				}

				pGenJavaClassFileWriter.appendSource("private native boolean");
				pGenJavaClassFileWriter.appendSource(" ");
				pGenJavaClassFileWriter.appendSource(callbackNativeMethodName);
				pGenJavaClassFileWriter.appendSource("(final long pAddress);"); // TODO Parameters
				pGenJavaClassFileWriter.appendSourceLine("");
			} else {
				System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
			}
		} else {
			System.err.println("Skipping callback: " + pClass.getSimpleName() + "." + methodName + " -> " + returnType);
		}
	}

	private void generateSetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

//		System.out.println("Generating setter: " + pClass.getSimpleName() + "." + pMethod.getName() + " -> " + returnType);
	}

	private void generateGetter(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();
		if((returnType == Byte.TYPE) || (returnType == Short.TYPE) || (returnType == Integer.TYPE) || (returnType == Long.TYPE) || (returnType == Float.TYPE) || (returnType == Double.TYPE) || (returnType == Boolean.TYPE)) {
//			System.out.println(pClass.getSimpleName() + "." + pMethod.getName() + " -> " + returnType);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}