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

	public GenCppClassFileWriter(final File pProxyCppRoot, final Class<?> pClass, final Util pUtil, final CppFormatter pCppFormatter) {
		this(pProxyCppRoot, pClass, pUtil, pCppFormatter, false);
	}

	public GenCppClassFileWriter(final File pProxyCppRoot, final Class<?> pClass, final Util pUtil, final CppFormatter pCppFormatter, final boolean pHeaderFileOnly) {
		if(pHeaderFileOnly) {
			this.mGenCppClassSourceFileWriter = null;
		} else {
			this.mGenCppClassSourceFileWriter = new GenFileWriter<GenCppClassSourceFileSegment>(pUtil.getGenCppClassSourceFile(pProxyCppRoot, pClass), pCppFormatter);
		}
		this.mGenCppClassHeaderFileWriter = new GenFileWriter<GenCppClassHeaderFileSegment>(pUtil.getGenCppClassHeaderFile(pProxyCppRoot, pClass), pCppFormatter);
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
		if(this.mGenCppClassSourceFileWriter != null) {
			this.mGenCppClassSourceFileWriter.begin();
		}
		this.mGenCppClassHeaderFileWriter.begin();
	}

	public void end() throws IOException {
		if(this.mGenCppClassSourceFileWriter != null) {
			this.mGenCppClassSourceFileWriter.end();
		}
		this.mGenCppClassHeaderFileWriter.end();
	}

	public GenFileWriterSegment append(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment, final String pString) {
		return this.mGenCppClassSourceFileWriter.append(pGenCppClassSourceFileSegment, pString);
	}

	public GenFileWriterSegment append(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment, final String pString, final Object ... pArguments) {
		return this.mGenCppClassSourceFileWriter.append(pGenCppClassSourceFileSegment, pString, pArguments);
	}

	public GenFileWriterSegment endLine(final GenCppClassSourceFileSegment pGenCppClassSourceFileSegment) {
		return this.mGenCppClassSourceFileWriter.endLine(pGenCppClassSourceFileSegment);
	}

	public GenFileWriterSegment append(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment, final String pString) {
		return this.mGenCppClassHeaderFileWriter.append(pGenCppClassHeaderFileSegment, pString);
	}

	public GenFileWriterSegment append(final GenCppClassHeaderFileSegment pGenCppClassHeaderFileSegment, final String pString, final Object ... pArguments) {
		return this.mGenCppClassHeaderFileWriter.append(pGenCppClassHeaderFileSegment, pString, pArguments);
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

		INCLUDES,
		STATICS,
		CLASS_INIT,
		JNI_EXPORTS,
		METHODS;

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
		METHODS_PUBLIC,
		METHODS_PROTECTED,
		METHODS_PRIVATE,
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
