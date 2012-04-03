package org.andengine.extension.scripting.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

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

	private final File mGenJavaRoot;
	private final File mGenCppRoot;
	private final JavaFormatter mGenJavaFormatter;
	private final CppFormatter mGenCppFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public EnumGenerator(final File pGenJavaRoot, final File pGenCppRoot, final JavaFormatter pGenJavaFormatter, final CppFormatter pGenCppFormatter, final List<String> pGenMethodsInclude, final Util pUtil) {
		super(pGenMethodsInclude, pUtil);

		this.mGenJavaRoot = pGenJavaRoot;
		this.mGenCppRoot = pGenCppRoot;
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

	public void generateEnumCode(final Class<?> pClass) throws IOException {
		final GenJavaClassFileWriter genJavaClassFileWriter = new GenJavaClassFileWriter(this.mGenJavaRoot, pClass, this.mUtil, this.mGenJavaFormatter);
		final GenCppClassFileWriter genCppClassFileWriter = new GenCppClassFileWriter(this.mGenCppRoot, pClass, this.mUtil, this.mGenCppFormatter);

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
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.PACKAGE, "package").space().append(genJavaClassPackageName).append(";").end();

			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTANTS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.CONSTRUCTORS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.FIELDS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.GETTERS_SETTERS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.METHODS);
			pGenJavaClassFileWriter.incrementIndent(GenJavaClassSourceFileSegment.STATIC_METHODS);

			/* Class. */
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "public").space();
			pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.CLASS_START, "class").space().append(genJavaClassName).append(" {").end();

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
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, "#include \"src/Wrapper.h\"").end();

				/* Externs. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "extern \"C\" {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.EXTERNS);

				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.EXTERNS, "JNIEXPORT void JNICALL ").append(genCppNativeInitClassJNIExportMethodName).append("(JNIEnv*, jclass);").end();

				/* Class. */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.CLASS_START, "class").space().append(genCppClassName).append(" : public Wrapper {").end();

				/* Methods. */
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "public:").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassHeaderFileSegment.METHODS_PUBLIC);

				/* Wrapper-Constructor */
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, genCppClassName).append("(jobject);").end();

				for(final Object enumConstant : pClass.getEnumConstants()) {
					pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.METHODS_PUBLIC, "static ").append(genCppClassName).append("* ").append(enumConstant.toString()).append(";").end();
				}
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

				/* Enum-Values. */
				final String jniSignatureType = this.mUtil.getJNIMethodSignatureType(pClass);
				for(final Object enumConstant : pClass.getEnumConstants()) {
					final String enumName = enumConstant.toString();
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.STATICS, genCppClassName).append("* ").append(genCppClassName).append("::").append(enumName).append(" = NULL;").end();

					final String jfieldIDLocalVariableName = genCppClassName + "_" + enumName + "_ID";
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, "jfieldID ").append(jfieldIDLocalVariableName).append(" = JNI_ENV()->GetStaticFieldID(").append(genCppStaticClassMemberName).append(", \"").append(enumName).append("\", \"").append(jniSignatureType).append("\");").end();
					pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.CLASS_INIT, genCppClassName).append("::").append(enumName).append(" = new(").append(genCppClassName).append("(JNI_ENV()->GetStaticObjectField(").append(genCppStaticClassMemberName).append(", ").append(jfieldIDLocalVariableName).append("));").end();
				}

				/* Wrapper-Constructor. */
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, genCppClassName).append("::").append(genCppClassName).append("(jobject p").append(genJavaClassName).append(") {").end();
				pGenCppClassFileWriter.incrementIndent(GenCppClassSourceFileSegment.METHODS);
				pGenCppClassFileWriter.append(GenCppClassSourceFileSegment.METHODS, "this->mUnwrapped = p").append(genJavaClassName).append(";").end();
				pGenCppClassFileWriter.decrementIndent(GenCppClassSourceFileSegment.METHODS);
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