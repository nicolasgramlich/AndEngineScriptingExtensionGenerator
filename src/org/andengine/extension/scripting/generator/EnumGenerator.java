package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassSourceFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter.GenJavaClassSourceFileSegment;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 14:24:02 - 03.04.2012
 */
public class EnumGenerator extends Generator {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final File mProxyJavaRoot;
	private final File mProxyCppRoot;
	private final JavaFormatter mProxyJavaFormatter;
	private final CppFormatter mProxyCppFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public EnumGenerator(final File pProxyJavaRoot, final File pProxyCppRoot, final JavaFormatter pGenJavaFormatter, final CppFormatter pGenCppFormatter, final Util pUtil) {
		super(pUtil);

		this.mProxyJavaRoot = pProxyJavaRoot;
		this.mProxyCppRoot = pProxyCppRoot;
		this.mProxyJavaFormatter = pGenJavaFormatter;
		this.mProxyCppFormatter = pGenCppFormatter;
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

	public void generateEnumCode(final Class<?> pClass) throws IOException {
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mProxyJavaRoot, pClass, this.mUtil, this.mProxyJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mProxyCppRoot, pClass, this.mUtil, this.mProxyCppFormatter);

		genJavaClassFileWriter.begin();
		genCppClassFileWriter.begin();

		this.generateEnumHeader(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateEnumMethods(pClass, genJavaClassFileWriter, genCppClassFileWriter);
		this.generateEnumFooter(pClass, genJavaClassFileWriter, genCppClassFileWriter);

		genJavaClassFileWriter.end();
		genCppClassFileWriter.end();
	}

	private void generateEnumHeader(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		final String genJavaClassName = this.mUtil.getGenJavaClassName(pClass);
		final String genCppClassName = this.mUtil.getGenCppClassName(pClass);
		final String genJavaClassPackageName = this.mUtil.getGenJavaClassPackageName(pClass);
		final String genCppNativeInitClassJNIExportMethodName = this.mUtil.getJNIExportMethodName(pClass, "initClass");

		/* Generate Java header. */
		{
			/* Package. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.PACKAGE, "package %s;", genJavaClassPackageName).end();

			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTANTS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.FIELDS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.GETTERS_SETTERS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.STATIC_METHODS);

			/* Class. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public class %s {", genJavaClassName).end();

			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.STATIC_METHODS, "public static native void nativeInitClass();").end();
		}

		/* Generate native header. */
		{
			/* Header. */
			{
				/* #ifdef. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#ifndef %s_H", genCppClassName).end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_IFDEF_HEAD, "#define %s_H", genCppClassName).end();

				/* Imports. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include <jni.h>").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/AndEngineScriptingExtension.h\"").end();
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end();

				/* Externs. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "extern \"C\" {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.EXTERNS);

				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT void JNICALL %s(JNIEnv*, jclass);", genCppNativeInitClassJNIExportMethodName).end();

				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class %s : public Wrapper {", genCppClassName).end();

				/* Methods. */
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "public:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);

				/* Wrapper-Constructor */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "%s(jobject);", genCppClassName).end();

				for(final Object enumConstant : pClass.getEnumConstants()) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "static %s* %s;", genCppClassName, enumConstant.toString()).end();
				}
			}

			/* Source. */
			{
				/* Includes. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, "#include <cstdlib>").end();
				final String genCppClassInclude = this.mUtil.getGenCppClassInclude(pClass);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.INCLUDES, genCppClassInclude).end();

				/* Statics. */
				final String genCppStaticClassMemberName = this.mUtil.getGenCppStaticClassMemberName(pClass, true);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "static jclass %s;", genCppStaticClassMemberName).end();

				/* Class init. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "JNIEXPORT void JNICALL %s(JNIEnv* pJNIEnv, jclass pJClass) {", genCppNativeInitClassJNIExportMethodName).end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.CLASS_INIT);

				final String genCppFullyQualifiedClassName = this.mUtil.getGenCppFullyQualifiedClassName(pClass, true);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "%s = (jclass)JNI_ENV()->NewGlobalRef(JNI_ENV()->FindClass(\"%s\"));", genCppStaticClassMemberName, genCppFullyQualifiedClassName).end();

				/* Enum-Values. */
				final String jniSignatureType = this.mUtil.getJNIMethodSignatureType(pClass);
				for(final Object enumConstant : pClass.getEnumConstants()) {
					final String enumName = enumConstant.toString();
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, "%s* %s::%s = NULL;", genCppClassName, genCppClassName, enumName).end();

					final String jfieldIDLocalVariableName = genCppClassName + "_" + enumName + "_ID";
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "jfieldID %s = JNI_ENV()->GetStaticFieldID(%s, \"%s\", \"%s\");", jfieldIDLocalVariableName, genCppStaticClassMemberName, enumName, jniSignatureType).end();
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "%s::%s = new %s(JNI_ENV()->GetStaticObjectField(%s, %s));", genCppClassName, enumName, genCppClassName, genCppStaticClassMemberName, jfieldIDLocalVariableName).end();
				}

				/* Wrapper-Constructor. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "%s::%s(jobject p%s) {", genCppClassName, genCppClassName, genJavaClassName).end();
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "\tthis->mUnwrapped = p%s;", genJavaClassName).end();
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "}").end();
			}
		}
	}

	private void generateEnumMethods(final Class<?> pClass, GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		// TODO
	}

	private void generateEnumFooter(final Class<?> pClass, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}