package org.hkijena.mcat.ui;

import org.hkijena.mcat.api.MCATProject;

import javax.swing.*;

/**
 * {@link JPanel} that contains a reference to a {@link MCATWorkbenchUI}
 */
public class MCATWorkbenchUIPanel extends JPanel {

    private final MCATWorkbenchUI workbenchUI;

    public MCATWorkbenchUIPanel(MCATWorkbenchUI workbenchUI) {
        this.workbenchUI = workbenchUI;
    }

    public MCATWorkbenchUI getWorkbenchUI() {
        return workbenchUI;
    }

    public MCATProject getProject() {
        return getWorkbenchUI().getProject();
    }
}