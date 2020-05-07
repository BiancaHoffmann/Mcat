package org.hkijena.mcat.extension.parameters.generators;

import org.scijava.Context;

/**
 * Generates {@link Float}
 */
public class FloatParameterGenerator extends NumberParameterGenerator {

    /**
     * Creates a new instance
     *
     * @param context the SciJava context
     */
    public FloatParameterGenerator(Context context) {
        super(context, Float.class);
    }
}