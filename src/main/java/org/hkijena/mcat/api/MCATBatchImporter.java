package org.hkijena.mcat.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Imports samples from a folder
 */
public class MCATBatchImporter {
    private MCATProject project;

    private Path inputFolder;
    private boolean subfoldersAreTreatments = true;
    private boolean includeTreatmentInName = false;

    private boolean importRawImages = true;
    private String rawImagesPattern = ".*\\.tif";

    private boolean importROI = true;
    private String roiPattern = ".*\\.roi";

    private boolean importPreprocessedImages = false;
    private String preprocessedImagesPattern = ".*\\.tif";

    private boolean importDerivationMatrix = false;
    private String derivationMatrixPattern = ".*\\.derivation_matrix.csv";

    private boolean importClusters = false;
    private String clustersPattern = ".*\\.clusters.csv";

    public MCATBatchImporter(MCATProject project) {
        this.project = project;
    }

    public Path getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(Path inputFolder) {
        this.inputFolder = inputFolder;
    }

    public boolean isSubfoldersAreTreatments() {
        return subfoldersAreTreatments;
    }

    public void setSubfoldersAreTreatments(boolean subfoldersAreTreatments) {
        this.subfoldersAreTreatments = subfoldersAreTreatments;
    }

    public boolean isIncludeTreatmentInName() {
        return includeTreatmentInName;
    }

    public void setIncludeTreatmentInName(boolean includeTreatmentInName) {
        this.includeTreatmentInName = includeTreatmentInName;
    }

    public boolean isImportRawImages() {
        return importRawImages;
    }

    public void setImportRawImages(boolean importRawImages) {
        this.importRawImages = importRawImages;
    }

    public String getRawImagesPattern() {
        return rawImagesPattern;
    }

    public void setRawImagesPattern(String rawImagesPattern) {
        this.rawImagesPattern = rawImagesPattern;
    }

    public boolean isImportROI() {
        return importROI;
    }

    public void setImportROI(boolean importROI) {
        this.importROI = importROI;
    }

    public String getRoiPattern() {
        return roiPattern;
    }

    public void setRoiPattern(String roiPattern) {
        this.roiPattern = roiPattern;
    }

    public boolean isImportPreprocessedImages() {
        return importPreprocessedImages;
    }

    public void setImportPreprocessedImages(boolean importPreprocessedImages) {
        this.importPreprocessedImages = importPreprocessedImages;
    }

    public boolean isImportDerivationMatrix() {
        return importDerivationMatrix;
    }

    public void setImportDerivationMatrix(boolean importDerivationMatrix) {
        this.importDerivationMatrix = importDerivationMatrix;
    }

    public String getDerivationMatrixPattern() {
        return derivationMatrixPattern;
    }

    public void setDerivationMatrixPattern(String derivationMatrixPattern) {
        this.derivationMatrixPattern = derivationMatrixPattern;
    }

    public String getPreprocessedImagesPattern() {
        return preprocessedImagesPattern;
    }

    public void setPreprocessedImagesPattern(String preprocessedImagesPattern) {
        this.preprocessedImagesPattern = preprocessedImagesPattern;
    }

    public boolean isImportClusters() {
        return importClusters;
    }

    public void setImportClusters(boolean importClusters) {
        this.importClusters = importClusters;
    }

    public String getClustersPattern() {
        return clustersPattern;
    }

    public void setClustersPattern(String clustersPattern) {
        this.clustersPattern = clustersPattern;
    }

    public MCATProject getProject() {
        return project;
    }

    private MCATSample importSample(Path folder, String treatment) {
        String name = folder.getFileName().toString();
        if(treatment != null && includeTreatmentInName) {
            name = treatment + "_" + name;
        }
        MCATSample sample = getProject().addSample(name);
        sample.getParameters().setTreatment(treatment);
        return sample;
    }

    public Set<MCATSample> run() throws IOException {
        Set<MCATSample> result = new HashSet<>();
        List<Path> treatmentPaths;

        if(subfoldersAreTreatments) {
           treatmentPaths = Files.list(inputFolder).filter(d -> Files.isDirectory(d)).collect(Collectors.toList());
        }
        else {
            treatmentPaths = Arrays.asList(inputFolder);
        }

        for(Path treatmentPath : treatmentPaths) {
            String treatmentName = treatmentPath.equals(inputFolder) ? null : treatmentPath.getFileName().toString();
            result.add(importSample(treatmentPath, treatmentName));
        }

        return result;
    }


}
