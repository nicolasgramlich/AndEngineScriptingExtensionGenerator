package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:48:25 - 20.03.2012
 */
public class InterfaceGenerator extends Generator {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final File mGenCppRoot;
	private final CppFormatter mGenCppFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public InterfaceGenerator(final File pGenCppRoot, final CppFormatter pGenCppFormatter, final List<String> pGenMethodsInclude, final Util pUtil) {
		super(pGenMethodsInclude, pUtil);

		this.mGenCppRoot = pGenCppRoot;
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

	public void generateInterfaceCode(final Class<?> pClass) throws IOException {
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mUtil, this.mGenCppFormatter, true);
		genCppClassFileWriter.begin();

		this.generateInterfaceHeader(pClass, genCppClassFileWriter);
		this.generateInterfaceMethods(pClass, genCppClassFileWriter);
		this.generateInterfaceFooter(pClass, genCppClassFileWriter);

		genCppClassFileWriter.end();
	}

	private void generateInterfaceHeader(final Class<?> pClass, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
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
						pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "public").space().append(this.mUtil.getGenCppClassName(interfaze));
					}
				}
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, " {").end();

				/* Methods. */
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "public:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual jobject unwrap() = 0;").end();
				
			}
		}
	}

	private void generateInterfaceMethods(final Class<?> pClass, final GenCppClassFileWriter pGenCppClassFileWriter) {
		for(final Method method : pClass.getMethods()) {
			if(this.isGenMethodIncluded(method)) {
				final String methodName = method.getName();
				if(methodName.startsWith("on")) {
					this.generateIncludes(method.getParameterTypes(), pGenCppClassFileWriter);
					this.generateInterfaceCallback(pClass, method, pGenCppClassFileWriter);
				} else {
					this.generateIncludes(method, pGenCppClassFileWriter);
					this.generateInterfaceMethod(pClass, method, pGenCppClassFileWriter);
				}
			}
		}
	}

	private void generateInterfaceMethod(final Class<?> pClass, final Method pMethod, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppMethodHeaderParamatersAsString = this.mUtil.getGenCppMethodHeaderParamatersAsString(pMethod);
		final String methodName = pMethod.getName();

		final String returnTypeName = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType());

		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "virtual").space().append(returnTypeName).space().append(methodName);
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "(");
		if(genCppMethodHeaderParamatersAsString != null) {
			pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppMethodHeaderParamatersAsString);
		}
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, ")");
		pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, " = 0;").end();
	}

	private void generateInterfaceCallback(final Class<?> pClass, final Method pMethod, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genCppMethodHeaderParamatersAsString = this.mUtil.getGenCppMethodHeaderParamatersAsString(pMethod);
		final String methodName = pMethod.getName();

		final String returnTypeName;
		if(pMethod.getReturnType() == Void.TYPE) {
			returnTypeName = this.mUtil.getGenCppParameterTypeName(Boolean.TYPE);
		} else {
			returnTypeName = this.mUtil.getGenCppParameterTypeName(pMethod.getReturnType());
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}