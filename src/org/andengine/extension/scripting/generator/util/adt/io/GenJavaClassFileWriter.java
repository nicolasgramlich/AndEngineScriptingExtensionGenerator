package org.andengine.extension.scripting.generator.util.adt.io;

import java.io.File;
import java.io.IOException;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.JavaFormatter;

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

	private final GenFileWriter mGenJavaClassSourceFileWriter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GenJavaClassFileWriter(final File pGenJavaRoot, final Class<?> pClass, final String pGenJavaClassSuffix, final JavaFormatter pJavaFormatter) {
		this.mGenJavaClassSourceFileWriter = new GenFileWriter(Util.getGenJavaClassSourceFile(pGenJavaRoot, pClass, pGenJavaClassSuffix), pJavaFormatter);
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

	public void appendSource(final String pString) {
		this.mGenJavaClassSourceFileWriter.append(pString);
	}

	public void appendSourceLine(final String pString) {
		this.mGenJavaClassSourceFileWriter.appendLine(pString);
	}

	public void end() throws IOException {
		this.mGenJavaClassSourceFileWriter.end();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
