package org.hkijena.mcat.ui;

import org.hkijena.mcat.api.MCATClusteringHierarchy;
import org.hkijena.mcat.api.parameters.MCATClusteringParameters;
import org.hkijena.mcat.ui.components.FormPanel;

import javax.swing.*;
import java.awt.*;

public class MCATClusteringUI extends MCATUIPanel {
    public MCATClusteringUI(MCATWorkbenchUI workbenchUI) {
        super(workbenchUI);
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        FormPanel formPanel = new FormPanel();

        MCATClusteringParameters parameters = getProject().getClusteringParameters();

        JComboBox<MCATClusteringHierarchy> clusteringHierarchyEditor = formPanel.addToForm(new JComboBox<>(MCATClusteringHierarchy.values()),
                new JLabel("Clustering hierarchy"),
                "documentation/parameter_clustering_hierarchy.md");
        clusteringHierarchyEditor.setSelectedItem(parameters.getClusteringHierarchy());
        clusteringHierarchyEditor.addActionListener(e -> parameters.setClusteringHierarchy((MCATClusteringHierarchy) clusteringHierarchyEditor.getSelectedItem()));

        JSpinner kMeansKEditor = formPanel.addToForm(new JSpinner(new SpinnerNumberModel(parameters.getkMeansK(), 1, Integer.MAX_VALUE, 1)),
                new JLabel("k-means groups"),
                null);
        kMeansKEditor.addChangeListener(e -> {
            parameters.setkMeansK((int)kMeansKEditor.getValue());
        });

        formPanel.addVerticalGlue();
        add(formPanel, BorderLayout.CENTER);
    }
}
