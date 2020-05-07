package org.hkijena.mcat.utils.api.registries;

import org.hkijena.mcat.extension.parameters.StandardParameterEditorsExtension;
import org.hkijena.mcat.utils.api.ACAQDefaultDocumentation;
import org.hkijena.mcat.utils.api.ACAQDocumentation;
import org.hkijena.mcat.utils.api.parameters.ACAQParameterAccess;
import org.hkijena.mcat.utils.ui.parameters.ACAQParameterEditorUI;
import org.hkijena.mcat.utils.ui.parameters.ACAQParameterGeneratorUI;
import org.scijava.Context;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Registry for parameter types
 */
public class ACAQUIParametertypeRegistry {

    private static ACAQUIParametertypeRegistry instance;

    private Map<Class<?>, ACAQDocumentation> parameterDocumentations = new HashMap<>();
    private Map<Class<?>, Class<? extends ACAQParameterEditorUI>> parameterTypes = new HashMap<>();

    private Map<Class<?>, Set<Class<? extends ACAQParameterGeneratorUI>>> parameterGenerators = new HashMap<>();
    private Map<Class<? extends ACAQParameterGeneratorUI>, ACAQDocumentation> parameterGeneratorDocumentations = new HashMap<>();

    /**
     * New instance
     */
    private ACAQUIParametertypeRegistry() {

    }

    /**
     * Registers a new parameter type
     *
     * @param parameterType parameter type
     * @param uiClass       corresponding editor UI
     */
    public void registerParameterEditor(Class<?> parameterType, Class<? extends ACAQParameterEditorUI> uiClass) {
        parameterTypes.put(parameterType, uiClass);
    }

    /**
     * Registers documentation for a parameter type
     *
     * @param parameterType parameter type
     * @param documentation the documentation
     */
    public void registerDocumentation(Class<?> parameterType, ACAQDocumentation documentation) {
        parameterDocumentations.put(parameterType, documentation);
    }

    /**
     * Gets documentation for a parameter type
     *
     * @param parameterType parameter type
     * @return documentation. Can be null.
     */
    public ACAQDocumentation getDocumentationFor(Class<?> parameterType) {
        return parameterDocumentations.getOrDefault(parameterType, null);
    }

    /**
     * Creates editor for the parameter
     *
     * @param context         SciJava context
     * @param parameterAccess the parameter
     * @return Parameter editor UI
     */
    public ACAQParameterEditorUI createEditorFor(Context context, ACAQParameterAccess parameterAccess) {
        Class<? extends ACAQParameterEditorUI> uiClass = parameterTypes.getOrDefault(parameterAccess.getFieldClass(), null);
        if (uiClass == null) {
            // Search a matching one
            for (Map.Entry<Class<?>, Class<? extends ACAQParameterEditorUI>> entry : parameterTypes.entrySet()) {
                if (entry.getKey().isAssignableFrom(parameterAccess.getFieldClass())) {
                    uiClass = entry.getValue();
                    break;
                }
            }
        }
        if (uiClass == null) {
            throw new NullPointerException("Could not find parameter editor for parameter class '" + parameterAccess.getFieldClass() + "'");
        }
        try {
            return uiClass.getConstructor(Context.class, ACAQParameterAccess.class).newInstance(context, parameterAccess);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if there is an editor for the parameter
     *
     * @param parameterType the parameter type
     * @return if there is an editor for the parameter
     */
    public boolean hasEditorFor(Class<?> parameterType) {
        return parameterTypes.containsKey(parameterType);
    }

    /**
     * Registers a UI that can generate parameters
     *
     * @param parameterClass Parameter class
     * @param uiClass        The generator UI class
     * @param name           Generator name
     * @param description    Description for the generator
     */
    public void registerGenerator(Class<?> parameterClass, Class<? extends ACAQParameterGeneratorUI> uiClass, String name, String description) {
        Set<Class<? extends ACAQParameterGeneratorUI>> generators = parameterGenerators.getOrDefault(parameterClass, null);
        if (generators == null) {
            generators = new HashSet<>();
            parameterGenerators.put(parameterClass, generators);
        }
        generators.add(uiClass);
        parameterGeneratorDocumentations.put(uiClass, new ACAQDefaultDocumentation(name, description));
    }

    /**
     * Returns all generators for the parameter class
     *
     * @param parameterClass the parameter class
     * @return Set of generators
     */
    public Set<Class<? extends ACAQParameterGeneratorUI>> getGeneratorsFor(Class<?> parameterClass) {
        return parameterGenerators.getOrDefault(parameterClass, Collections.emptySet());
    }

    /**
     * Returns documentation for the generator
     *
     * @param generatorClass the generator
     * @return documentation
     */
    public ACAQDocumentation getGeneratorDocumentationFor(Class<? extends ACAQParameterGeneratorUI> generatorClass) {
        return parameterGeneratorDocumentations.get(generatorClass);
    }

    public static ACAQUIParametertypeRegistry getInstance() {
        if (instance == null) {
            instance = new ACAQUIParametertypeRegistry();
            StandardParameterEditorsExtension.register();
        }

        return instance;
    }
}
