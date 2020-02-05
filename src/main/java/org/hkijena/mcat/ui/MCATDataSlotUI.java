package org.hkijena.mcat.ui;

import org.hkijena.mcat.api.MCATDataProvider;
import org.hkijena.mcat.api.MCATDataSlot;
import org.hkijena.mcat.api.MCATProjectSample;
import org.hkijena.mcat.ui.registries.MCATDataProviderUIRegistry;
import org.hkijena.mcat.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * UI for a {@link MCATDataSlot}
 */
public class MCATDataSlotUI extends JPanel {
    private MCATProjectSample sample;
    private MCATDataSlot<?> slot;
    private MCATDataProviderUI<?> currentProviderUI = null;
    private JButton selectionButton;

    public MCATDataSlotUI(MCATProjectSample sample, MCATDataSlot<?> slot) {
        this.sample = sample;
        this.slot = slot;
        slot.ensureDataProvider();
        setLayout(new BorderLayout());
        initializeDataProviderSelection();
        createDataProviderUI();
    }

    private void initializeDataProviderSelection() {
        selectionButton = new JButton(slot.getCurrentProvider().getName(), UIUtils.getIconFromResources("database.png"));
        selectionButton.setHorizontalAlignment(SwingConstants.LEFT);
        JPopupMenu menu = UIUtils.addPopupMenuToComponent(selectionButton);
        for(MCATDataProvider<?> provider : slot.getAvailableProviders()) {
            JMenuItem item = new JMenuItem(provider.getName(), UIUtils.getIconFromResources("database.png"));
            menu.add(item);
        }

        add(selectionButton, BorderLayout.EAST);
    }

    private void createDataProviderUI() {
        if(currentProviderUI != null)
            remove(currentProviderUI);

        currentProviderUI = MCATDataProviderUIRegistry.getInstance().getUIFor(sample, slot.getCurrentProvider());
        add(currentProviderUI, BorderLayout.CENTER);
    }


    public JButton getSelectionButton() {
        return selectionButton;
    }
}
