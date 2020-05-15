package org.hkijena.mcat.api.parameters;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.eventbus.EventBus;
import org.hkijena.mcat.api.MCATDocumentation;
import org.hkijena.mcat.api.MCATPostprocessingMethod;
import org.hkijena.mcat.api.events.ParameterChangedEvent;

import java.util.Objects;

/**
 * Additional parameters used to generate an {@link org.hkijena.mcat.extension.datatypes.AUCData}
 */
public class MCATAUCDataConditions implements MCATParameterCollection{
    private EventBus eventBus = new EventBus();
    private MCATPostprocessingMethod method;

    public MCATAUCDataConditions() {
    }

    public MCATAUCDataConditions(MCATPostprocessingMethod method) {
        this.method = method;
    }

    @MCATDocumentation(name = "AUC generating method")
    @MCATParameter("auc-method")
    @JsonGetter("auc-method")
    public MCATPostprocessingMethod getMethod() {
        return method;
    }

    @MCATParameter("auc-method")
    @JsonSetter("auc-method")
    public void setMethod(MCATPostprocessingMethod method) {
        this.method = method;
        eventBus.post(new ParameterChangedEvent(this, "auc-method"));
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCATAUCDataConditions that = (MCATAUCDataConditions) o;
        return method == that.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }
}