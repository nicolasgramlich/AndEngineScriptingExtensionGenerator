package org.andengine.extension.scripting.generator.util.adt.io;

import java.io.File;
import java.io.IOException;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenFileWriter.GenFileWriterSegment;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:02:38 - 21.03.2012
 */
public class GenJavaClassFileWriter {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final GenFileWriter<GenJavaClassSourceFileSegment> mGenJavaClassSourceFileWriter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GenJavaClassFileWriter(final File pGenJavaRoot, final Class<?> pClass, final Util pUtil, final JavaFormatter pJavaFormatter) {
		this.mGenJavaClassSourceFileWriter = new GenFileWriter<GenJavaClassSourceFileSegment>(pUtil.getGenJavaClassSourceFile(pGenJavaRoot, pClass), pJavaFormatter);
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

	public void begin() {
		this.mGenJavaClassSourceFileWriter.begin();
	}

	public void end() throws IOException {
		this.mGenJavaClassSourceFileWriter.end();
	}

	public GenFileWriterSegment append(final GenJavaClassSourceFileSegment pGenJavaClassSourceFileSegment, final String pString) {
		return this.mGenJavaClassSourceFileWriter.append(pGenJavaClassSourceFileSegment, pString);
	}

	public GenFileWriterSegment append(final GenJavaClassSourceFileSegment pGenJavaClassSourceFileSegment, final String pString, final Object ... pArguments) {
		return this.mGenJavaClassSourceFileWriter.append(pGenJavaClassSourceFileSegment, pString, pArguments);
	}

	public GenFileWriterSegment endLine(final GenJavaClassSourceFileSegment pGenJavaClassSourceFileSegment) {
		return this.mGenJavaClassSourceFileWriter.endLine(pGenJavaClassSourceFileSegment);
	}

	public GenFileWriterSegment incrementIndent(final GenJavaClassSourceFileSegment pGenJavaClassSourceFileSegment) {
		return this.mGenJavaClassSourceFileWriter.incrementIndent(pGenJavaClassSourceFileSegment);
	}

	public GenFileWriterSegment decrementIndent(final GenJavaClassSourceFileSegment pGenJavaClassSourceFileSegment) {
		return this.mGenJavaClassSourceFileWriter.decrementIndent(pGenJavaClassSourceFileSegment);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static enum GenJavaClassSourceFileSegment {
		// ===========================================================
		// Elements
		// ===========================================================

		PACKAGE,
		IMPORTS,
		CLASS_START,
		STATIC_METHODS,
		CONSTANTS,
		FIELDS,
		CONSTRUCTORS,
		GETTERS_SETTERS,
		METHODS,
		CLASS_END;

		// ===========================================================
		// Constants
		// ===========================================================

		// ===========================================================
		// Fields
		// ===========================================================

		// ===========================================================
		// Constructors
		// ===========================================================

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		// ===========================================================
		// Methods
		// ===========================================================

		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}
}
