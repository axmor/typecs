package com.axmor.eclipse.typescript.editor.color;

import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.*;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * A color registry for syntax highlight
 * 
 * @author Asya Vorobyova
 *
 */
public enum TypeScriptColorRegistry {
    COLOR_REGISTRY;
    /**
     * Create a singleton instance
     */
    private ColorRegistry registry = new ColorRegistry();

    /**
     * A constructor
     */
    private TypeScriptColorRegistry() {
        registry.put(TS_REFERENCE, new RGB(90, 90, 90));
        registry.put(TS_KEYWORD, new RGB(127, 0, 85));
        registry.put(TS_JAVA_DOC, new RGB(63, 95, 121));
        registry.put(TS_NUMBER, new RGB(51, 0, 102));
        registry.put(TS_COMMENT, new RGB(63, 127, 25));
        registry.put(TS_STRING, new RGB(42, 0, 255));
    }

    /**
     * Gets color by id
     * 
     * @param name a color id
     * @return a color
     */
    public Color get(String name) {
        return registry.get(name);
    }
}
