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
		this.mGenCppClassSourceFileWriter = new GenFileWriter(Util.getGenCppClassSourceFile(pGenJavaRoot, pClass), pCppFormatter);
		this.mGenCppClassHeaderFileWriter = new GenFileWriter(Util.getGenCppClassHeaderFile(pGenJavaRoot, pClass), pCppFormatter);
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

	public GenFileWriter appendSource(final String pString) {
		this.mGenCppClassSourceFileWriter.append(pString);
		return this.mGenCppClassSourceFileWriter;
	}

	public GenFileWriter appendSourceLine(final String pString) {
		this.mGenCppClassSourceFileWriter.appendLine(pString);
		return this.mGenCppClassSourceFileWriter;
	}

	public GenFileWriter endSourceLine() {
		this.mGenCppClassSourceFileWriter.endLine();
		return this.mGenCppClassSourceFileWriter;
	}

	public GenFileWriter appendHeader(final String pString) {
		this.mGenCppClassHeaderFileWriter.append(pString);
		return this.mGenCppClassHeaderFileWriter;
	}

	public GenFileWriter appendHeaderLine(final String pString) {
		this.mGenCppClassHeaderFileWriter.appendLine(pString);
		return this.mGenCppClassHeaderFileWriter;
	}

	public GenFileWriter endHeaderLine() {
		this.mGenCppClassHeaderFileWriter.endLine();
		return this.mGenCppClassHeaderFileWriter;
	}

	public void end() throws IOException {
		this.mGenCppClassSourceFileWriter.end();
		this.mGenCppClassHeaderFileWriter.end();
	}

	public void incrementHeaderIndent() {
		this.mGenCppClassHeaderFileWriter.incrementIndent();
	}

	public void incrementSourceIndent() {
		this.mGenCppClassSourceFileWriter.incrementIndent();
	}

	public void decrementHeaderIndent() {
		this.mGenCppClassHeaderFileWriter.decrementIndent();
	}

	public void decrementSourceIndent() {
		this.mGenCppClassSourceFileWriter.decrementIndent();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
