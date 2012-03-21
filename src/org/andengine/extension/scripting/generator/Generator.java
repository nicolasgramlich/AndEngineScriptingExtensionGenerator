package org.andengine.extension.scripting.generator;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

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

	@Option(required = true, name = "-gen-java-class-suffix")
	private String mGenJavaClassSuffix;

	@Option(required = true, name = "-gen-method-exclude", multiValued = true)
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
			final File classSourceFile = this.getInJavaClassSourceFile(className);
			if(!classSourceFile.exists()) {
				throw new IllegalArgumentException("'" + classSourceFile + "' does not exist!");
			}
			final File classFile = this.getInJavaClassFile(className);
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

	private File getInJavaClassSourceFile(final String pFullyQualifiedClassName) {
		return new File(this.mInJavaRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".java");
	}

	private File getInJavaClassFile(final String pFullyQualifiedClassName) {
		return new File(this.mInJavaBinRootClasses, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".class");
	}

	private File getGenJavaClassSourceFile(final String pFullyQualifiedClassName) {
		return new File(this.mGenJavaRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + this.mGenJavaClassSuffix + ".java");
	}

	private File getGenCppClassSourceFile(final String pFullyQualifiedClassName) {
		return new File(this.mGenCppRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".cpp");
	}

	private File getGenCppClassHeaderFile(final String pFullyQualifiedClassName) {
		return new File(this.mGenCppRoot, pFullyQualifiedClassName.replace('.', File.separatorChar) + ".h");
	}

	private static String getMethodModifiersAsString(final Method pMethod) {
		final StringBuilder modifiersBuilder = new StringBuilder();

		final int modifiers = pMethod.getModifiers();
		if(Modifier.isPublic(modifiers)) {
			modifiersBuilder.append("public ");
		} else if(Modifier.isProtected(modifiers)) {
			modifiersBuilder.append("protetcted ");
		} else if(Modifier.isPrivate(modifiers)) {
			modifiersBuilder.append("private ");
		}

		return modifiersBuilder.toString().trim();
	}

	private void generateCode() {
		for(final String className : this.mFullyQualifiedClassNames) {
			try {
				final URI uri = this.mInJavaBinRootClasses.toURI();
				final ClassLoader classLoader = new URLClassLoader(new URL[]{uri.toURL()});

				final Class<?> clazz = classLoader.loadClass(className);

				this.parseClass(clazz);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void parseClass(final Class<?> pClazz) {
		for(final Method method : pClazz.getMethods()) {
			if(!this.isGenMethodExcluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("get")) {
					this.generateGetter(pClazz, method);
				} else if(methodName.startsWith("set")) {
					this.generateSetter(pClazz, method);
				} else if(methodName.startsWith("on")) {
					this.generateCallback(pClazz, method);
				} else {
//					System.err.println("Skipping method: " + pClazz.getSimpleName() + "." + methodName + "(...) !");
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

	private void generateCallback(final Class<?> pClazz, final Method pMethod) {
		final Class<?> returnType = pMethod.getReturnType();

		final String methodName = pMethod.getName();
		if((returnType == Boolean.TYPE) || (returnType == Void.TYPE)) {
			if(Modifier.isPublic(pMethod.getModifiers())) {
				final String visibilityModifier = Generator.getMethodModifiersAsString(pMethod);
				final String callbackNativeMethodName = this.generateCallbackNativeMethodName(pMethod);

				System.out.println("@Override");
				System.out.print(visibilityModifier);
				System.out.print(" ");
				System.out.print(returnType.getSimpleName());
				System.out.print(" ");
				System.out.print(methodName);
				System.out.println("() {"); // TODO Parameters
				
				if(returnType == Void.TYPE) {
					System.out.print("\tif(!this.");
					System.out.print(callbackNativeMethodName);
					System.out.println("(this.mAddress)) {"); // TODO Parameters
					System.out.print("\t\tsuper.");
					System.out.print(methodName);
					System.out.println("();"); // TODO Parameters
					System.out.println("\t}");
					System.out.println("}");
				} else {
					System.out.print("\tif(!this.");
					System.out.print(callbackNativeMethodName);
					System.out.println("(this.mAddress)) {"); // TODO Parameters
					System.out.println("\t\treturn true;");
					System.out.println(methodName);
					System.out.println("();"); // TODO Parameters
					System.out.println("\t}");
					System.out.println("}");
				}

				System.out.print("private native boolean");
				System.out.print(" ");
				System.out.print(callbackNativeMethodName);
				System.out.print("(final long pAddress);"); // TODO Parameters
				System.out.println("");
			} else {
				System.out.println("Skipping callback: " + pClazz.getSimpleName() + "." + methodName + " -> " + returnType);
			}
		} else {
			System.err.println("Skipping callback: " + pClazz.getSimpleName() + "." + methodName + " -> " + returnType);
		}
	}

	private String generateCallbackNativeMethodName(final Method pMethod) {
		final String methodName = pMethod.getName();
		return "native" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1, methodName.length());
	}

	private void generateSetter(final Class<?> pClazz, final Method pMethod) {
		final Class<?> returnType = pMethod.getReturnType();

		System.out.println("Generating setter: " + pClazz.getSimpleName() + "." + pMethod.getName() + " -> " + returnType);
	}

	private void generateGetter(final Class<?> pClazz, final Method pMethod) {
		final Class<?> returnType = pMethod.getReturnType();
		if(returnType == Byte.TYPE || returnType == Short.TYPE || returnType == Integer.TYPE || returnType == Long.TYPE || returnType == Float.TYPE || returnType == Double.TYPE || returnType == Boolean.TYPE) {
			System.out.println(pClazz.getSimpleName() + "." + pMethod.getName() + " -> " + returnType);
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}