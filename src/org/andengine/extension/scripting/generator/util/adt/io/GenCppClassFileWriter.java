package org.andengine.extension.scripting.generator.util.adt.io;

import java.io.File;
import java.io.IOException;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.CppFormatter;

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

	private final GenFileWriter mGenCppClassSourceFileWriter;
	private final GenFileWriter mGenCppClassHeaderFileWriter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GenCppClassFileWriter(final File pGenJavaRoot, final Class<?> pClass, final CppFormatter pCppFormatter) {
		this.mGenCppClassSourceFileWriter = new GenFileWriter(Util.getGenCppClassHeaderFile(pGenJavaRoot, pClass), pCppFormatter);
		this.mGenCppClassHeaderFileWriter = new GenFileWriter(Util.getGenCppClassSourceFile(pGenJavaRoot, pClass), pCppFormatter);
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

	public void appendSource(final String pString) throws IOException {
		this.mGenCppClassSourceFileWriter.append(pString);
	}

	public void appendSourceLine(final String pString) throws IOException {
		this.mGenCppClassSourceFileWriter.appendLine(pString);
	}

	public void appendHeader(final String pString) throws IOException {
		this.mGenCppClassHeaderFileWriter.append(pString);
	}

	public void appendHeaderLine(final String pString) throws IOException {
		this.mGenCppClassHeaderFileWriter.appendLine(pString);
	}

	public void end() throws IOException {
		this.mGenCppClassSourceFileWriter.end();
		this.mGenCppClassHeaderFileWriter.end();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
