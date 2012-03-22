package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
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

				this.generateClassCode(clazz);
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

		this.generateClassBoilerplate(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassHeader(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassFields(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassConstructors(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateClassFooter(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateClassBoilerplate(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassPackageName = Util.getGenJavaClassPackageName(pClass);
		final String genCppClassName = Util.getGenCppClassName(pClass);
		
		/* Generate Java boilerplate. */
		{
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
		}
		
		/* Generate native boilerplate. */
		{
			/* #ifdef. */
			pGenCppClassFileWriter.appendHeaderLine("#ifndef " + genCppClassName + "_H");
			pGenCppClassFileWriter.appendHeaderLine("#define " + genCppClassName + "_H");
			pGenCppClassFileWriter.endHeaderLine();
			
			/* Imports. */
			pGenCppClassFileWriter.appendHeaderLine("#include <jni.h>");
			pGenCppClassFileWriter.appendHeaderLine("#include \"src/ScriptingEnvironment.h\"");
			pGenCppClassFileWriter.appendHeaderLine("#include \"src/Wrapper.h\""); // TODO Import 'Superclass.h' instead of Wrapper.h
			pGenCppClassFileWriter.endHeaderLine();
		}
	}

	private void generateClassHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);
		final String genCppClassName = Util.getGenCppClassName(pClass);

		/* Generate Java header. */
		{
			/* Class. */
			pGenJavaClassFileWriter.appendSource("public class");
			pGenJavaClassFileWriter.appendSource(" ");
			pGenJavaClassFileWriter.appendSource(genJavaClassName);
			pGenJavaClassFileWriter.appendSource(" ");
			pGenJavaClassFileWriter.appendSource("extends");
			pGenJavaClassFileWriter.appendSource(" ");
			pGenJavaClassFileWriter.appendSource(pClass.getSimpleName());
			pGenJavaClassFileWriter.appendSourceLine(" {");
			pGenJavaClassFileWriter.incrementSourceIndent();

			pGenJavaClassFileWriter.appendSourceLine("public static native void nativeInitClass();");
		}

		/* Generate native header. */
		{
			/* Class. */
			pGenCppClassFileWriter.appendHeader("class").space().append(genCppClassName).append(" : ").append("public").space().append("Wrapper").space().append("{").endLine(); // TODO extend 'Superclass' insteaf of Wra
			pGenCppClassFileWriter.incrementHeaderIndent();
		}
	}

	private void generateClassFooter(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		pGenJavaClassFileWriter.decrementSourceIndent();
		pGenJavaClassFileWriter.appendSourceLine("}");

		pGenCppClassFileWriter.decrementHeaderIndent();
		pGenCppClassFileWriter.appendHeaderLine("}");
		pGenCppClassFileWriter.appendHeaderLine("#endif");
	}

	private void generateClassFields(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		pGenJavaClassFileWriter.appendSourceLine("private final long mAddress;");
	}

	private void generateClassConstructors(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = Util.getGenJavaClassName(pClass, this.mGenJavaClassSuffix);

		for(final Constructor<?> constructor : pClass.getConstructors()) {
			if(!Modifier.isPrivate(constructor.getModifiers())) {
				pGenJavaClassFileWriter.appendSource(Util.getConstructorModifiersAsString(constructor));
				pGenJavaClassFileWriter.appendSource(" ");
				pGenJavaClassFileWriter.appendSource(genJavaClassName);
				pGenJavaClassFileWriter.appendSourceLine("(final long pAddress) {"); // TODO Parameters
				pGenJavaClassFileWriter.appendSourceLine("\tsuper();"); // TODO Parameters
				pGenJavaClassFileWriter.endSourceLine();
				pGenJavaClassFileWriter.appendSourceLine("\tthis.mAddress = pAddress;");

				pGenJavaClassFileWriter.appendSourceLine("}");
			}
		}
	}

	private void generateClassMethods(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
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

	private void generateCallback(final Class<?> pClass, final Method pMethod, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final Class<?> returnType = pMethod.getReturnType();

		final String methodName = pMethod.getName();
		if((returnType == Boolean.TYPE) || (returnType == Void.TYPE)) {
			if(Modifier.isPublic(pMethod.getModifiers())) { // TODO Is this check correct?
				/* Generate Java side of the callback. */
				final String javaNativeMethodName = Util.getJavaNativeMethodName(pMethod);
				final String cppMethodName = Util.getCppJNIMethodFullyQualifiedName(pMethod, this.mGenJavaClassSuffix);

				{
					/* Source. */
					final String visibilityModifier = Util.getMethodModifiersAsString(pMethod);
					pGenJavaClassFileWriter.appendSourceLine("@Override");
					pGenJavaClassFileWriter.appendSource(visibilityModifier);
					pGenJavaClassFileWriter.appendSource(" ");
					pGenJavaClassFileWriter.appendSource(returnType.getSimpleName());
					pGenJavaClassFileWriter.appendSource(" ");
					pGenJavaClassFileWriter.appendSource(methodName);
					pGenJavaClassFileWriter.appendSourceLine("() {"); // TODO Parameters
	
					if(returnType == Void.TYPE) {
						pGenJavaClassFileWriter.appendSource("\tif(!this.");
						pGenJavaClassFileWriter.appendSource(javaNativeMethodName);
						pGenJavaClassFileWriter.appendSourceLine("(this.mAddress)) {"); // TODO Parameters
						pGenJavaClassFileWriter.appendSource("\t\tsuper.");
						pGenJavaClassFileWriter.appendSource(methodName);
						pGenJavaClassFileWriter.appendSourceLine("();"); // TODO Parameters
						pGenJavaClassFileWriter.appendSourceLine("\t}");
						pGenJavaClassFileWriter.appendSourceLine("}");
					} else {
						pGenJavaClassFileWriter.appendSource("\tif(!this.");
						pGenJavaClassFileWriter.appendSource(javaNativeMethodName);
						pGenJavaClassFileWriter.appendSourceLine("(this.mAddress)) {"); // TODO Parameters
						pGenJavaClassFileWriter.appendSourceLine("\t\treturn true;");
						pGenJavaClassFileWriter.appendSourceLine(methodName);
						pGenJavaClassFileWriter.appendSourceLine("();"); // TODO Parameters
						pGenJavaClassFileWriter.appendSourceLine("\t}");
						pGenJavaClassFileWriter.appendSourceLine("}");
					}
	
					pGenJavaClassFileWriter.appendSource("private native boolean");
					pGenJavaClassFileWriter.appendSource(" ");
					pGenJavaClassFileWriter.appendSource(javaNativeMethodName);
					pGenJavaClassFileWriter.appendSource("(final long pAddress);"); // TODO Parameters
					pGenJavaClassFileWriter.endSourceLine();
				}

				/* Generate native side of the callback. */
				{
					/* Header. */
					pGenCppClassFileWriter.appendHeaderLine("extern \"C\" {");
					pGenCppClassFileWriter.incrementHeaderIndent();
					pGenCppClassFileWriter.appendHeader("JNIEXPORT jboolean JNICALL");
					pGenCppClassFileWriter.appendHeader(" ");
					pGenCppClassFileWriter.appendHeader(cppMethodName);
					pGenCppClassFileWriter.appendHeader("(");
					pGenCppClassFileWriter.appendHeader("JNIEnv*, jobject, jlong"); // TODO Parameters
					pGenCppClassFileWriter.appendHeader(");");
					pGenCppClassFileWriter.endHeaderLine();
					pGenCppClassFileWriter.decrementHeaderIndent();
					pGenCppClassFileWriter.appendHeaderLine("}");
	
					/* Source. */
	//				JNIEXPORT jboolean JNICALL Java_org_andengine_extension_scripting_entity_Entity_nativeOnAttached(JNIEnv* pJNIEnv, jobject pJObject, jlong pAddress) {
	//					Entity* entity = (Entity*)pAddress;
	//
	//					return entity->onAttached();
	//				}
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