package org.andengine.extension.scripting.generator.util.adt.io;

import java.io.File;
import java.io.IOException;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;
import org.andengine.extension.scripting.generator.util.adt.io.GenFileWriter.GenFileWriterSegment;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:02:38 - 21.03.2012
 */
public class GenCppClassFileWriter {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final GenFileWriter<GenCppClassSourceFileSegment> mGenCppClassSourceFileWriter;
	private final GenFileWriter<GenCppClassHeaderFileSegment> mGenCppClassHeaderFileWriter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GenCppClassFileWriter(final File pGenCppRoot, final Class<?> pClass, final String pGenCppClassSuffix, final CppFormatter pCppFormatter) {
		this.mGenCppClassSourceFileWriter = new GenFileWriter<GenCppClassSourceFileSegment>(Util.getGenCppClassSourceFile(pGenCppRoot, pClass, pGenCppClassSuffix), pCppFormatter);
		this.mGenCppClassHeaderFileWriter = new GenFileWriter<GenCppClassHeaderFileSegment>(Util.getGenCppClassHeaderFile(pGenCppRoot, pClass, pGenCppClassSuffix), pCppFormatter);
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

	public void begin() throws IOException {
		this.mGenCppClassSourceFileWriter.begin();
		this.mGenCppClassHeaderFileWriter.begin();
	}

	public void end() throws IOException {
		this.mGenCppClassSourceFileWriter.end();
		this.mGenCppClassHeaderFileWriter.end();
	}

	public GenFileWriterSegment append(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment, final String pString) {
		return this.mGenCppClassSourceFileWriter.append(pGenCppClassSourceFileSegment, pString);
	}

	public GenFileWriterSegment appendLine(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment, final String pString) {
		return this.mGenCppClassSourceFileWriter.appendLine(pGenCppClassSourceFileSegment, pString);
	}

	public GenFileWriterSegment endLine(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment) {
		return this.mGenCppClassSourceFileWriter.endLine(pGenCppClassSourceFileSegment);
	}

	public GenFileWriterSegment append(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment, final String pString) {
		return this.mGenCppClassHeaderFileWriter.append(pGenCppClassHeaderFileSegment, pString);
	}

	public GenFileWriterSegment appendLine(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment, final String pString) {
		return this.mGenCppClassHeaderFileWriter.appendLine(pGenCppClassHeaderFileSegment, pString);
	}

	public GenFileWriterSegment endLine(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment) {
		return this.mGenCppClassHeaderFileWriter.endLine(pGenCppClassHeaderFileSegment);
	}

	public GenFileWriterSegment incrementIndent(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment) {
		return this.mGenCppClassHeaderFileWriter.incrementIndent(pGenCppClassHeaderFileSegment);
	}

	public GenFileWriterSegment incrementIndent(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment) {
		return this.mGenCppClassSourceFileWriter.incrementIndent(pGenCppClassSourceFileSegment);
	}

	public GenFileWriterSegment decrementIndent(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment) {
		return this.mGenCppClassHeaderFileWriter.decrementIndent(pGenCppClassHeaderFileSegment);
	}

	public GenFileWriterSegment decrementIndent(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment) {
		return this.mGenCppClassSourceFileWriter.decrementIndent(pGenCppClassSourceFileSegment);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static enum GenCppClassSourceFileSegment {
		// ===========================================================
		// Elements
		// ===========================================================

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

	public static enum GenCppClassHeaderFileSegment {
		// ===========================================================
		// Elements
		// ===========================================================

		CLASS_IFDEF_HEAD,
		INCLUDES,
		EXTERNS,
		CLASS_START,
		METHODS_PRIVATE,
		METHODS_PROTECTED,
		METHODS_PUBLIC,
		CLASS_END,
		CLASS_IFDEF_FOOT;
		
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
