package org.hkijena.mcat.extension.dataproviders.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.eventbus.EventBus;
import org.hkijena.mcat.api.MCATDataProvider;
import org.hkijena.mcat.extension.parameters.editors.FilePathParameterSettings;
import org.hkijena.mcat.ui.components.FileSelection;
import org.hkijena.mcat.api.MCATDocumentation;
import org.hkijena.mcat.api.MCATValidityReport;
import org.hkijena.mcat.api.events.ParameterChangedEvent;
import org.hkijena.mcat.api.parameters.MCATParameter;
import org.hkijena.mcat.api.parameters.MCATParameterCollection;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FileDataProvider implements MCATParameterCollection, MCATDataProvider {
    private EventBus eventBus = new EventBus();
    private Path filePath;

    public FileDataProvider() {

    }

    public FileDataProvider(FileDataProvider other) {
        this.filePath = other.filePath;
    }

    @Override
    public MCATDataProvider duplicate() {
        try {
            return getClass().getConstructor(getClass()).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @MCATDocumentation(name = "File path")
    @MCATParameter("file-path")
    @JsonGetter("file-path")
    @FilePathParameterSettings(ioMode = FileSelection.IOMode.Open, pathMode = FileSelection.PathMode.FilesOnly)
    public Path getFilePath() {
        return filePath;
    }

    @MCATParameter("file-path")
    @JsonSetter("file-path")
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
        eventBus.post(new ParameterChangedEvent(this, "file-path"));
    }

    @Override
    public boolean isValid() {
        return filePath != null && Files.exists(filePath);
    }

    @Override
    public void reportValidity(MCATValidityReport report) {
        if (!Files.exists(filePath)) {
            report.forCategory("File path").reportIsInvalid("File path is invalid!",
                    "The selected file '" + filePath + "' does not exist!",
                    "Please select a valid file.",
                    this);
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }
}
