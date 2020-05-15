package org.hkijena.mcat.extension.resultanalysis;

import ij.IJ;
import ij.ImagePlus;
import org.hkijena.mcat.api.MCATResultDataInterfaces;
import org.hkijena.mcat.ui.MCATWorkbenchUI;
import org.hkijena.mcat.utils.UIUtils;

import java.nio.file.Path;

public class HyperStackDataSlotResultUI extends MCATDefaultDataSlotResultUI {
    public HyperStackDataSlotResultUI(MCATWorkbenchUI workbenchUI, Path outputPath, MCATResultDataInterfaces.SlotEntry slot) {
        super(workbenchUI, outputPath, slot);
    }

    @Override
    protected void registerActions() {
        super.registerActions();
        Path tiffFile = findFirstFileWithExtension(".tif");
        if (tiffFile != null) {
            registerAction("Open in ImageJ", "Opens the image file in ImageJ", UIUtils.getIconFromResources("imagej.png"), ui -> {
                ImagePlus img = IJ.openImage(tiffFile.toString());
                if (img != null) {
                    img.show();
                    img.setTitle(tiffFile.getFileName().toString());
                }
            });
        }
    }
}
