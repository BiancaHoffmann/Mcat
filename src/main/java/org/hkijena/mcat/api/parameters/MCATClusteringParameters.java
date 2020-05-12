package org.hkijena.mcat.api.parameters;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.eventbus.EventBus;
import org.hkijena.mcat.api.MCATClusteringHierarchy;
import org.hkijena.mcat.api.MCATDocumentation;
import org.hkijena.mcat.api.events.ParameterChangedEvent;

import java.util.Objects;

/**
 * Class that contains all clustering parameters.
 * <p>
 * To create a parameter, create a a private field with getter & setter.
 * Annotate the getter with {@link JsonGetter}, {@link MCATParameter}, and {@link MCATDocumentation}
 * Annotate the setter with {@link MCATParameter} and {@link JsonSetter}
 * <p>
 * Post an event {@link ParameterChangedEvent} when a value is set.
 * <p>
 * Add the variable to getHashCode() and equals()
 */
public class MCATClusteringParameters implements MCATParameterCollection {
    private EventBus eventBus = new EventBus();
    private int kMeansK = 5;
    private int minLength = Integer.MAX_VALUE;

    private MCATClusteringHierarchy clusteringHierarchy = MCATClusteringHierarchy.PerTreatment;

    public MCATClusteringParameters() {
    }

    public MCATClusteringParameters(MCATClusteringParameters other) {
        this.kMeansK = other.kMeansK;
        this.minLength = other.minLength;
        this.clusteringHierarchy = other.clusteringHierarchy;
    }

    @MCATDocumentation(name = "Clustering hierarchy")
    @MCATParameter(value = "clustering-hierarchy", shortKey = "ch")
    @JsonGetter("clustering-hierarchy")
    public MCATClusteringHierarchy getClusteringHierarchy() {
        return clusteringHierarchy;
    }

    @MCATParameter("clustering-hierarchy")
    @JsonSetter("clustering-hierarchy")
    public void setClusteringHierarchy(MCATClusteringHierarchy clusteringHierarchy) {
        this.clusteringHierarchy = clusteringHierarchy;
        eventBus.post(new ParameterChangedEvent(this, "clustering-hierarchy"));
    }

    @MCATDocumentation(name = "K-Means groups (K)")
    @MCATParameter(value = "kmeans-k", shortKey = "k")
    @JsonGetter("kmeans-k")
    public int getkMeansK() {
        return kMeansK;
    }

    @MCATParameter("kmeans-k")
    @JsonSetter("kmeans-k")
    public boolean setkMeansK(int kMeansK) {
        if(kMeansK <= 0) {
            return false;
        }
        this.kMeansK = kMeansK;
        eventBus.post(new ParameterChangedEvent(this, "kmeans-k"));
        return true;
    }

    @MCATDocumentation(name = "Minimum length")
    @MCATParameter(value = "min-length", shortKey = "mlength")
    @JsonGetter("min-length")
    public int getMinLength() {
        return minLength;
    }

    @MCATParameter("min-length")
    @JsonSetter("min-length")
    public void setMinLength(int minLength) {
        this.minLength = minLength;
        eventBus.post(new ParameterChangedEvent(this, "min-length"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCATClusteringParameters that = (MCATClusteringParameters) o;
        return kMeansK == that.kMeansK &&
                minLength == that.minLength &&
                clusteringHierarchy == that.clusteringHierarchy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kMeansK, minLength, clusteringHierarchy);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public String toString() {
        return MCATCustomParameterCollection.parametersToString((new MCATTraversedParameterCollection(this)).getParameters().values(), ",", "=");
    }
}
