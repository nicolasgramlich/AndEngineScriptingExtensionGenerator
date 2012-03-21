package org.andengine.extension.scripting.generator.util.adt.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 15:02:38 - 21.03.2012
 */
public class GenFileWriter {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final StringBuilder mStringBuilder = new StringBuilder();
	private final File mFile;
	private final IFormatter mFormatter;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GenFileWriter(final File pFile, final IFormatter pFormatter) {
		this.mFile = pFile;
		this.mFormatter = pFormatter;
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
		this.mStringBuilder.setLength(0);
	}

	public void append(final String pString) {
		this.mStringBuilder.append(pString);
	}

	public void appendLine(final String pString) {
		this.mStringBuilder.append(pString);
		this.mStringBuilder.append('\n');
	}

	public void end() throws IOException {
		final Writer writer = new FileWriter(this.mFile);

		if(this.mFormatter == null) {
			writer.write(this.mStringBuilder.toString());
		} else {
			writer.write(this.mFormatter.format(this.mStringBuilder.toString()));
		}

		writer.flush();

		try {
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
