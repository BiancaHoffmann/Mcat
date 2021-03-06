/*******************************************************************************
 * Copyright by Dr. Bianca Hoffmann, Ruman Gerst, Dr. Zoltán Cseresnyés and Prof. Dr. Marc Thilo Figge
 * 
 * Research Group Applied Systems Biology - Head: Prof. Dr. Marc Thilo Figge
 * https://www.leibniz-hki.de/en/applied-systems-biology.html
 * HKI-Center for Systems Biology of Infection
 * Leibniz Institute for Natural Product Research and Infection Biology - Hans Knöll Insitute (HKI)
 * Adolf-Reichwein-Straße 23, 07745 Jena, Germany
 * 
 * The project code is licensed under BSD 2-Clause.
 * See the LICENSE file provided with the code for the full license.
 ******************************************************************************/
package org.hkijena.mcat.api.parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hkijena.mcat.api.events.ParameterStructureChangedEvent;
import org.hkijena.mcat.utils.JsonUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

/**
 * Holds a user-definable set of parameters
 */
public class MCATDynamicParameterCollection implements MCATCustomParameterCollection {

    private EventBus eventBus = new EventBus();
    private BiMap<String, MCATMutableParameterAccess> dynamicParameters = HashBiMap.create();
    private Set<Class<?>> allowedTypes = new HashSet<>();
    private String name;
    private String description;
    private boolean allowUserModification = true;
    private boolean delayEvents = false;

    /**
     * Creates a new instance with user modification enabled
     *
     * @param allowedTypes The parameter types that can be added
     */
    public MCATDynamicParameterCollection(Class<?>... allowedTypes) {
        this.allowedTypes.addAll(Arrays.asList(allowedTypes));
    }

    /**
     * Creates a new instance
     *
     * @param allowUserModification let user modify this collection
     * @param allowedTypes          The parameter types that can be added by the user (ignored if user cannot add)
     */
    public MCATDynamicParameterCollection(boolean allowUserModification, Class<?>... allowedTypes) {
        this.allowUserModification = allowUserModification;
        this.allowedTypes.addAll(Arrays.asList(allowedTypes));
    }

    /**
     * Copies the parameters from the original
     *
     * @param other The original
     */
    public MCATDynamicParameterCollection(MCATDynamicParameterCollection other) {
        this.allowedTypes.addAll(other.allowedTypes);
        this.allowUserModification = other.allowUserModification;
        for (Map.Entry<String, MCATMutableParameterAccess> entry : other.dynamicParameters.entrySet()) {
            MCATMutableParameterAccess parameterAccess = new MCATMutableParameterAccess(entry.getValue());
            parameterAccess.setParameterHolder(this);
            dynamicParameters.put(entry.getKey(), parameterAccess);
        }
    }

    @Override
    public Map<String, MCATParameterAccess> getParameters() {
        return Collections.unmodifiableMap(dynamicParameters);
    }

    @JsonGetter("parameters")
    private Map<String, MCATMutableParameterAccess> getDynamicParameters() {
        return Collections.unmodifiableMap(dynamicParameters);
    }

    @JsonSetter("parameters")
    private void setDynamicParameters(Map<String, MCATMutableParameterAccess> dynamicParameters) {
        this.dynamicParameters.putAll(dynamicParameters);
    }

    /**
     * Adds a new parameter.
     * The parameterHolder attribute is changed
     *
     * @param parameterAccess the parameter
     * @return the parameter access
     */
    public MCATMutableParameterAccess addParameter(MCATMutableParameterAccess parameterAccess) {
        if (dynamicParameters.containsKey(parameterAccess.getKey()))
            throw new IllegalArgumentException("Parameter with key " + parameterAccess.getKey() + " already exists!");
        parameterAccess.setParameterHolder(this);
        dynamicParameters.put(parameterAccess.getKey(), parameterAccess);
        if (!delayEvents)
            getEventBus().post(new ParameterStructureChangedEvent(this));
        return parameterAccess;
    }

    /**
     * Adds a new parameter
     *
     * @param key        A unique key
     * @param fieldClass The parameter class
     * @return The created parameter access
     */
    public MCATMutableParameterAccess addParameter(String key, Class<?> fieldClass) {
        MCATMutableParameterAccess parameterAccess = new MCATMutableParameterAccess(this, key, fieldClass);
        parameterAccess.setName(key);
        return addParameter(parameterAccess);
    }

    /**
     * Removes all parameters
     */
    public void clear() {
        dynamicParameters.clear();
        if (!delayEvents)
            getEventBus().post(new ParameterStructureChangedEvent(this));
    }

    /**
     * Removes a parameter
     *
     * @param key The parameter key
     */
    public void removeParameter(String key) {
        dynamicParameters.remove(key);
        if (!delayEvents)
            getEventBus().post(new ParameterStructureChangedEvent(this));
    }

    /**
     * Gets a parameter by its key
     *
     * @param key The parameter key
     * @return The parameter access
     */
    public MCATMutableParameterAccess getParameter(String key) {
        return dynamicParameters.get(key);
    }

    /**
     * Gets a parameter value
     *
     * @param key The parameter key
     * @param <T> The parameter type
     * @return The parameter value
     */
    public <T> T getValue(String key) {
        return getParameter(key).get();
    }

    /**
     * Sets a parameter value
     *
     * @param key   The parameter key
     * @param value The parameter value
     * @param <T>   The parameter type
     * @return True if setting the value was successful
     */
    public <T> boolean setValue(String key, T value) {
        return getParameter(key).set(value);
    }

    /**
     * @return Allowed parameter types
     */
    public Set<Class<?>> getAllowedTypes() {
        return allowedTypes;
    }

    /**
     * Sets allowed parameter types
     *
     * @param allowedTypes Parameter types
     */
    public void setAllowedTypes(Set<Class<?>> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    /**
     * @return Parameter holder name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Parameter holder description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Loads the holder from JSON
     *
     * @param node JSON data
     */
    public void fromJson(JsonNode node) {
        dynamicParameters.clear();
        JsonNode parametersNode = node.get("parameters");
        for (Map.Entry<String, JsonNode> entry : ImmutableList.copyOf(parametersNode.fields())) {
            try {
                MCATMutableParameterAccess parameterAccess = JsonUtils.getObjectMapper().readerFor(MCATMutableParameterAccess.class).readValue(entry.getValue());
                parameterAccess.setKey(entry.getKey());
                parameterAccess.setParameterHolder(this);
                dynamicParameters.put(entry.getKey(), parameterAccess);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return True if modification by users is allowed
     */
    public boolean isAllowUserModification() {
        return allowUserModification;
    }

    /**
     * Enabled/disables if modification by users is allowed
     *
     * @param allowUserModification True if modification is allowed
     */
    public void setAllowUserModification(boolean allowUserModification) {
        this.allowUserModification = allowUserModification;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Returns true if there is a parameter with given key
     *
     * @param key the parameter key
     * @return if there is a parameter with given key
     */
    public boolean containsKey(String key) {
        return dynamicParameters.containsKey(key);
    }

    /**
     * Returns the parameter with key
     *
     * @param key the parameter key
     * @return the parameter access
     */
    public MCATParameterAccess get(String key) {
        return dynamicParameters.get(key);
    }

    /**
     * Halts all {@link ParameterStructureChangedEvent} until endModificationBlock() is called.
     * Use this method for many changes at once
     */
    public void beginModificationBlock() {
        if (delayEvents)
            throw new UnsupportedOperationException("Modification block already started!");
        delayEvents = true;
    }

    /**
     * Ends a modification block started by beginModificationBlock().
     * Triggers a {@link ParameterStructureChangedEvent}
     */
    public void endModificationBlock() {
        if (!delayEvents)
            throw new UnsupportedOperationException("No modification block!");
        delayEvents = false;
        eventBus.post(new ParameterStructureChangedEvent(this));
    }

    /**
     * Copies the parameter configuration into the target
     *
     * @param target the target configuration
     */
    public void copyTo(MCATDynamicParameterCollection target) {
        target.setAllowUserModification(isAllowUserModification());
        target.setAllowedTypes(getAllowedTypes());
        target.beginModificationBlock();
        target.clear();
        for (Map.Entry<String, MCATMutableParameterAccess> entry : dynamicParameters.entrySet()) {
            target.addParameter(new MCATMutableParameterAccess(entry.getValue()));
        }
        target.endModificationBlock();
    }

}
