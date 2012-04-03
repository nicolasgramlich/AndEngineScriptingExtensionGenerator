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
	protected final List<String> mGenMethodsInclude;

	// ===========================================================
	// Constructors
	// ===========================================================

	public Generator(final List<String> pGenMethodsInclude, final Util pUtil) {
		this.mGenMethodsInclude = pGenMethodsInclude;
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

	protected boolean isGenMethodIncluded(final Method pMethod) {
		final String methodName = pMethod.getName();
		for(final String genMethodInclude : this.mGenMethodsInclude) {
			if(genMethodInclude.equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	protected void generateParameterImportsAndIncludes(final AccessibleObject pAccessibleObject, final GenJavaClassFileWriter pGenJavaClassFileWriter, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;
			final Class<?>[] parameterTypes = constructor.getParameterTypes();

			this.generateImports(parameterTypes, pGenJavaClassFileWriter);
			this.generateIncludes(constructor.getParameterTypes(), pGenCppClassFileWriter);
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();

			this.generateImports(parameterTypes, pGenJavaClassFileWriter);
			this.generateIncludes(parameterTypes, pGenCppClassFileWriter);
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected void generateIncludes(final AccessibleObject pAccessibleObject, final GenCppClassFileWriter pGenCppClassFileWriter) {
		if(pAccessibleObject instanceof Constructor<?>) {
			final Constructor<?> constructor = (Constructor<?>)pAccessibleObject;

			this.generateIncludes(constructor.getParameterTypes(), pGenCppClassFileWriter);
		} else if(pAccessibleObject instanceof Method) {
			final Method method = (Method)pAccessibleObject;
			final Class<?>[] parameterTypes = method.getParameterTypes();

			this.generateIncludes(parameterTypes, pGenCppClassFileWriter);
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected void generateImports(final Class<?>[] pTypes, final GenJavaClassFileWriter pGenJavaClassFileWriter) {
		for(final Class<?> type : pTypes) {
			if(!this.mUtil.isPrimitiveType(type)) {
				final String genJavaImportClassName = this.mUtil.getGenJavaClassImport(type);
				pGenJavaClassFileWriter.append(GenJavaClassSourceFileSegment.IMPORTS, genJavaImportClassName).end();
			}
		}
	}

	protected void generateIncludes(final Class<?>[] pTypes, final GenCppClassFileWriter pGenCppClassFileWriter) {
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