package org.andengine.extension.scripting.generator;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.andengine.extension.scripting.generator.util.Util;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenCppClassFileWriter.GenCppClassHeaderFileSegment;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter;
import org.andengine.extension.scripting.generator.util.adt.io.GenJavaClassFileWriter.GenJavaClassSourceFileSegment;

/**
 * (c) Zynga 2012
 *
 * @author Nicolas Gramlich <ngramlich@zynga.com>
 * @since 16:48:25 - 20.03.2012
 */
public class Generator {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Util mUtil;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Generator(final Util pUtil) {
		this.mUtil = pUtil;
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

	protected void generateParameterImportsAndIncludes(final AccessibleObject pAccessibleObject, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;
			final Class<?>[] parameterTypes = constructor.getParameterTypes();

			this.generateImports(pGenJavaClassFileWriter, parameterTypes);
			this.generateIncludes(pGenCppClassFileWriter, parameterTypes);
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();

			this.generateImports(pGenJavaClassFileWriter, parameterTypes);
			this.generateIncludes(pGenCppClassFileWriter, parameterTypes);

			this.generateImports(pGenJavaClassFileWriter, method.getReturnType());
			this.generateIncludes(pGenCppClassFileWriter, method.getReturnType());
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected void generateIncludes(final GenCppClassFileWriter pGenCppClassFileWriter, final AccessibleObject pAccessibleObject) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;

			this.generateIncludes(pGenCppClassFileWriter, constructor.getParameterTypes());
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();

			this.generateIncludes(pGenCppClassFileWriter, parameterTypes);
			this.generateIncludes(pGenCppClassFileWriter, method.getReturnType());
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected void generateImports(final GenJavaClassFileWriter pGenJavaClassFileWriter, final Class<?> ... pTypes) {
		for(final Class<?> type : pTypes) {
			if(!this.mUtil.isPrimitiveType(type)) {
				final String genJavaImportClassName = this.mUtil.getGenJavaClassImport(type);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, genJavaImportClassName).end();
			}
		}
	}

	protected void generateIncludes(final GenCppClassFileWriter pGenCppClassFileWriter, final Class<?> ... pTypes) {
		for(final Class<?> type : pTypes) {
			if(!this.mUtil.isPrimitiveType(type)) {
				final String genCppIncludeClassName = this.mUtil.getGenCppClassInclude(type);
				pGenCppClassFileWriter.append(GenCppClassHeaderFileSegment.INCLUDES, genCppIncludeClassName).end();
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}