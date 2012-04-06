package org.andengine.extension.scripting.generator;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 11:32:51 - 03.04.2012
 */
public class AndEngineScriptingExtensionGenerator {
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
	private String mGenCppClassSuffix = "";

	@Option(required = false, name = "-gen-cpp-formatter")
	private CppFormatter mGenCppFormatter;

	@Option(required = false, name = "-gen-method-include", multiValued = true)
	private List<String> mGenMethodsInclude;

	@Option(required = false, name = "-gen-class-exclude", multiValued = true)
	private List<String> mGenClassesExclude;

	@Option(required = true, name = "-class", multiValued = true)
	private List<String> mFullyQualifiedClassNames;

	private final File mInJavaBinRootClasses;

	private final Util mUtil;

	// ===========================================================
	// Constructors
	// ===========================================================

	public static void main(final String[] pArgs) throws Exception {
		new AndEngineScriptingExtensionGenerator(pArgs);
	}


	public AndEngineScriptingExtensionGenerator(final String[] pArgs) throws CmdLineException {
		final CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(pArgs);

		this.mInJavaBinRootClasses = new File(this.mInJavaBinRoot, "classes/");
		this.mUtil = new Util(this.mGenJavaClassSuffix, this.mGenCppClassSuffix, this.mGenMethodsInclude, this.mGenClassesExclude);

		this.checkArguments();

		this.generateCode();
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
			final File classSourceFile = this.mUtil.getInJavaClassSourceFile(this.mInJavaRoot, className);
			if(!classSourceFile.exists()) {
				throw new IllegalArgumentException("'" + classSourceFile + "' does not exist!");
			}
			final File classFile = this.mUtil.getInJavaClassFile(this.mInJavaBinRootClasses, className);
			if(!classFile.exists()) {
				throw new IllegalArgumentException("'" + classFile + "' does not exist!");
			}
		}
	}

	private void generateCode() {
		for(final String className : this.mFullyQualifiedClassNames) {
			try {
				final URI uri = this.mInJavaBinRootClasses.toURI();
				final ClassLoader classLoader = new URLClassLoader(new URL[]{uri.toURL()});

				final Class<?> clazz = classLoader.loadClass(className);

				System.out.print("Generating: '" + className + "' ...");
				if(clazz.isInterface()) {
					new InterfaceGenerator(this.mGenCppRoot, this.mGenCppFormatter, this.mUtil).generateInterfaceCode(clazz);
				} else if(clazz.isEnum()) {
					new EnumGenerator(this.mGenJavaRoot, this.mGenCppRoot, this.mGenJavaFormatter, this.mGenCppFormatter, this.mUtil).generateEnumCode(clazz);
				} else {
					new ClassGenerator(this.mGenJavaRoot, this.mGenCppRoot, this.mGenJavaFormatter, this.mGenCppFormatter, this.mUtil).generateClassCode(clazz);
				}
				System.out.println(" done!");
			} catch (final Throwable t) {
				t.printStackTrace();
				System.out.println(" ERROR!");
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
