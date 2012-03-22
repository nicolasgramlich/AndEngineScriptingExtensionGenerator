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
	private final StringBuilder mLineBuilder = new StringBuilder();
	private final File mFile;
	private final IFormatter mFormatter;
	private int mIndent;

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
		this.mLineBuilder.setLength(0);
	}

	public GenFileWriter append(final String pString) {
		this.mLineBuilder.append(pString);
		return this;
	}
	
	public GenFileWriter space() {
		this.mLineBuilder.append(' ');
		return this;
	}

	public GenFileWriter appendLine(final String pString) {
		this.append(pString);
		this.endLine();
		return this;
	}

	public GenFileWriter endLine() {
		for(int i = 0; i < this.mIndent; i++) {
			this.mStringBuilder.append('\t');
		}
		this.mStringBuilder.append(this.mLineBuilder);
		this.mStringBuilder.append('\n');
		this.mLineBuilder.setLength(0);
		return this;
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

	public void incrementIndent() {
		this.mIndent++;
	}

	public void decrementIndent() {
		if(this.mIndent == 0) {
			throw new IllegalStateException();
		}
		this.mIndent--;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
