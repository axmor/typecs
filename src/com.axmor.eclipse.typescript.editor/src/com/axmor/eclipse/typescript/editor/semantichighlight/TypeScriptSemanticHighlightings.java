package com.axmor.eclipse.typescript.editor.semantichighlight;

import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_BOLD_SUFFIX;
import static com.axmor.eclipse.typescript.editor.parser.TypeScriptTokenConstants.TS_ITALIC_SUFFIX;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;

import com.axmor.eclipse.typescript.core.Activator;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

public class TypeScriptSemanticHighlightings {

    /**
     * A named preference part that controls the highlighting of property.
     */
    public static final String CLASS_PROPERTY = "ts_classProperty";

    /**
     * A named preference part that controls the highlighting of classes.
     */
    public static final String CLASS = "ts_class";

    /**
     * A named preference part that controls the highlighting of modules.
     */
    public static final String MODULE = "ts_module";

    /**
     * A named preference part that controls the highlighting of methods.
     */
    public static final String METHOD = "ts_method";

    /**
     * A named preference part that controls the highlighting of interfaces.
     */
    public static final String INTERFACE = "ts_interface";

    /**
     * A named preference part that controls the highlighting of local variables.
     */
    public static final String LOCAL_VARIABLE = "ts_localVariable";

    public static final String SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX = ".enabled";

    public static final String EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX = "semanticHighlighting.";

    /**
     * Semantic highlightings
     */
    private static TypeScriptSemanticHighlighting[] fgSemanticHighlightings;

    /**
     * Semantic highlighting for classes.
     */
    private static final class ClassHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return CLASS;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Classes";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("class") || obj.get("type").equals("constructor")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    /**
     * Semantic highlighting for interfaces.
     */
    private static final class InterfaceHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return INTERFACE;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Interfaces";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("interface")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    /**
     * Semantic highlighting for properties.
     */
    private static final class PropertyHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return CLASS_PROPERTY;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Properties";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("property")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    /**
     * Semantic highlighting for modules.
     */
    private static final class ModuleHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return MODULE;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Modules";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("module")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    /**
     * Semantic highlighting for local variables.
     */
    private static final class LocalVariablesHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return LOCAL_VARIABLE;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Local variables";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("local var")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    /**
     * Semantic highlighting for methods.
     */
    private static final class MethodHighlighting extends TypeScriptSemanticHighlighting {

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#getPreferenceKey()
         */
        @Override
        public String getPreferenceKey() {
            return METHOD;
        }

        /*
         * @see org.eclipse.jdt.internal.ui.javaeditor.ISemanticHighlighting#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return "Methods";
        }

        /*
         * @see
         * org.eclipse.jdt.internal.ui.javaeditor.SemanticHighlighting#consumes(org.eclipse.jdt.
         * internal.ui.javaeditor.SemanticToken)
         */
        @Override
        public boolean consumes(JSONObject obj) {
            try {
                if (obj.get("type").equals("method")) {
                    return true;
                }
            } catch (JSONException e) {
                Activator.error(e);
            }
            return false;
        }

        @Override
        public RGB getDefaultTextColor() {
            return new RGB(0, 0, 0);
        }

        @Override
        public boolean isBoldByDefault() {
            return false;
        }

        @Override
        public boolean isItalicByDefault() {
            return false;
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    public static String getColorPreferenceKey(TypeScriptSemanticHighlighting semanticHighlighting) {
        return EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey();
    }

    public static String getBoldPreferenceKey(TypeScriptSemanticHighlighting semanticHighlighting) {
        return EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + TS_BOLD_SUFFIX;
    }

    public static String getItalicPreferenceKey(TypeScriptSemanticHighlighting semanticHighlighting) {
        return EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey() + TS_ITALIC_SUFFIX;
    }

    public static String getEnabledPreferenceKey(TypeScriptSemanticHighlighting semanticHighlighting) {
        return EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + semanticHighlighting.getPreferenceKey()
                + SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
    }

    /**
     * @return The semantic highlighting, the order defines the precedence of matches, the first
     *         match wins.
     */
    public static TypeScriptSemanticHighlighting[] getSemanticHighlightings() {
        if (fgSemanticHighlightings == null)
            fgSemanticHighlightings = new TypeScriptSemanticHighlighting[] { new ClassHighlighting(),
                    new InterfaceHighlighting(), new MethodHighlighting(), new ModuleHighlighting(),
                    new PropertyHighlighting(), new LocalVariablesHighlighting() };
        return fgSemanticHighlightings;
    }

    /**
     * Tests whether semantic highlighting is currently enabled.
     */
    public static boolean isEnabled(IPreferenceStore store) {
        TypeScriptSemanticHighlighting[] highlightings = getSemanticHighlightings();
        boolean enable = false;
        for (int i = 0; i < highlightings.length; i++) {
            String enabledKey = getEnabledPreferenceKey(highlightings[i]);
            if (store.getBoolean(enabledKey)) {
                enable = true;
                break;
            }
        }

        return enable;
    }

    /**
     * Tests whether <code>event</code> in <code>store</code> affects the enablement of semantic
     * highlighting.
     */
    public static boolean affectsEnablement(IPreferenceStore store, PropertyChangeEvent event) {
        String relevantKey = null;
        TypeScriptSemanticHighlighting[] highlightings = getSemanticHighlightings();
        for (int i = 0; i < highlightings.length; i++) {
            if (event.getProperty().equals(getEnabledPreferenceKey(highlightings[i]))) {
                relevantKey = event.getProperty();
                break;
            }
        }
        if (relevantKey == null) {
            return false;
        }

        for (int i = 0; i < highlightings.length; i++) {
            String key = getEnabledPreferenceKey(highlightings[i]);
            if (key.equals(relevantKey)) {
                continue;
            }
            if (store.getBoolean(key)) {
                return false; // another is still enabled or was enabled before
            }
        }

        // all others are disabled, so toggling relevantKey affects the enablement
        return true;
    }

    /**
     * Initialize default preferences in the given preference store.
     *
     * @param store
     *            The preference store
     */
    public static void initDefaults(IPreferenceStore store) {
        TypeScriptSemanticHighlighting[] semanticHighlightings = getSemanticHighlightings();
        for (int i = 0, n = semanticHighlightings.length; i < n; i++) {
            TypeScriptSemanticHighlighting semanticHighlighting = semanticHighlightings[i];
            setDefaultAndFireEvent(store, TypeScriptSemanticHighlightings.getColorPreferenceKey(semanticHighlighting),
                    semanticHighlighting.getDefaultTextColor());
            store.setDefault(TypeScriptSemanticHighlightings.getBoldPreferenceKey(semanticHighlighting),
                    semanticHighlighting.isBoldByDefault());
            store.setDefault(TypeScriptSemanticHighlightings.getItalicPreferenceKey(semanticHighlighting),
                    semanticHighlighting.isItalicByDefault());
            store.setDefault(TypeScriptSemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting),
                    semanticHighlighting.isEnabledByDefault());
        }

    }

    /**
     * Sets the default value and fires a property change event if necessary.
     */
    private static void setDefaultAndFireEvent(IPreferenceStore store, String key, RGB newValue) {
        RGB oldValue = null;
        if (store.isDefault(key)) {
            oldValue = PreferenceConverter.getDefaultColor(store, key);
        }

        PreferenceConverter.setDefault(store, key, newValue);

        if (oldValue != null && !oldValue.equals(newValue)) {
            store.firePropertyChangeEvent(key, oldValue, newValue);
        }
    }
}
