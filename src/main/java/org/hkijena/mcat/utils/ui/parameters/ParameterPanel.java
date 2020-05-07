package org.hkijena.mcat.utils.ui.parameters;

import com.google.common.eventbus.Subscribe;
import com.google.common.html.HtmlEscapers;
import org.hkijena.mcat.ui.components.FormPanel;
import org.hkijena.mcat.ui.components.MarkdownDocument;
import org.hkijena.mcat.utils.ResourceUtils;
import org.hkijena.mcat.utils.UIUtils;
import org.hkijena.mcat.utils.api.ACAQDocumentation;
import org.hkijena.mcat.utils.api.events.ParameterStructureChangedEvent;
import org.hkijena.mcat.utils.api.parameters.*;
import org.hkijena.mcat.utils.api.registries.ACAQUIParametertypeRegistry;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UI around a {@link ACAQParameterCollection}
 */
public class ParameterPanel extends FormPanel implements Contextual {
    /**
     * Flag for {@link ParameterPanel}. Makes that no group headers are created.
     * This includes dynamic parameter group headers that contain buttons for modification.
     */
    public static final int NO_GROUP_HEADERS = 64;
    /**
     * Flag for {@link ParameterPanel}. Makes that group headers without name or description or special functionality (like
     * dynamic parameters) are not shown. Overridden by NO_GROUP_HEADERS.
     */
    public static final int NO_EMPTY_GROUP_HEADERS = 128;
    private Context context;
    private ACAQParameterCollection parameterCollection;
    private boolean noGroupHeaders;
    private boolean noEmptyGroupHeaders;

    /**
     * @param context             SciJava context
     * @param parameterCollection Object containing the parameters
     * @param documentation       Optional documentation
     * @param flags               Flags
     */
    public ParameterPanel(Context context, ACAQParameterCollection parameterCollection, MarkdownDocument documentation, int flags) {
        super(documentation, flags);
        this.noGroupHeaders = (flags & NO_GROUP_HEADERS) == NO_GROUP_HEADERS;
        this.noEmptyGroupHeaders = (flags & NO_EMPTY_GROUP_HEADERS) == NO_EMPTY_GROUP_HEADERS;
        this.context = context;
        this.parameterCollection = parameterCollection;
        reloadForm();
        this.parameterCollection.getEventBus().register(this);
    }

    /**
     * Reloads the form
     */
    public void reloadForm() {
        clear();
        ACAQTraversedParameterCollection parameterCollection = new ACAQTraversedParameterCollection(getParameterCollection());

        Map<ACAQParameterCollection, List<ACAQParameterAccess>> groupedBySource = parameterCollection.getGroupedBySource();
        if (groupedBySource.containsKey(this.parameterCollection)) {
            addToForm(parameterCollection, this.parameterCollection, groupedBySource.get(this.parameterCollection));
        }

        for (ACAQParameterCollection collection : groupedBySource.keySet().stream().sorted(
                Comparator.nullsFirst(Comparator.comparing(parameterCollection::getSourceDocumentationName)))
                .collect(Collectors.toList())) {
            if (collection == this.parameterCollection)
                continue;
            addToForm(parameterCollection, collection, groupedBySource.get(collection));
        }
        addVerticalGlue();
    }

    private void addToForm(ACAQTraversedParameterCollection parameterCollection, ACAQParameterCollection parameterHolder, List<ACAQParameterAccess> parameterAccesses) {
        boolean isModifiable = parameterHolder instanceof ACAQDynamicParameterCollection && ((ACAQDynamicParameterCollection) parameterHolder).isAllowUserModification();

        if (!isModifiable && parameterAccesses.isEmpty())
            return;

        if (!noGroupHeaders) {
            ACAQDocumentation documentation = parameterCollection.getSourceDocumentation(parameterHolder);
            boolean documentationIsEmpty = documentation == null || (StringUtils.isNullOrEmpty(documentation.name()) && StringUtils.isNullOrEmpty(documentation.description()));

            if (!noEmptyGroupHeaders || (!documentationIsEmpty && !isModifiable)) {
                GroupHeaderPanel groupHeaderPanel = addGroupHeader(parameterCollection.getSourceDocumentationName(parameterHolder),
                        UIUtils.getIconFromResources("cog.png"));

                if (documentation != null && !StringUtils.isNullOrEmpty(documentation.description())) {
                    groupHeaderPanel.getDescriptionArea().setVisible(true);
                    groupHeaderPanel.getDescriptionArea().setText(documentation.description());
                }

                if (isModifiable) {
                    JButton addButton = new JButton(UIUtils.getIconFromResources("add.png"));
                    initializeAddDynamicParameterButton(addButton, (ACAQDynamicParameterCollection) parameterHolder);
                    addButton.setToolTipText("Add new parameter");
                    UIUtils.makeFlat25x25(addButton);
                    groupHeaderPanel.addColumn(addButton);
                }
            }
        }

        List<ACAQParameterEditorUI> uiList = new ArrayList<>();
        for (ACAQParameterAccess parameterAccess : parameterAccesses) {
            ACAQParameterEditorUI ui = ACAQUIParametertypeRegistry.getInstance().createEditorFor(getContext(), parameterAccess);
            uiList.add(ui);
        }
        for (ACAQParameterEditorUI ui : uiList.stream().sorted(Comparator.comparing((ACAQParameterEditorUI u) -> !u.isUILabelEnabled())
                .thenComparing(u -> u.getParameterAccess().getName())).collect(Collectors.toList())) {
            ACAQParameterAccess parameterAccess = ui.getParameterAccess();
            JPanel labelPanel = new JPanel(new BorderLayout());
            if (ui.isUILabelEnabled())
                labelPanel.add(new JLabel(parameterAccess.getName()), BorderLayout.CENTER);
            if (isModifiable) {
                JButton removeButton = new JButton(UIUtils.getIconFromResources("close-tab.png"));
                removeButton.setToolTipText("Remove this parameter");
                UIUtils.makeBorderlessWithoutMargin(removeButton);
                removeButton.addActionListener(e -> removeDynamicParameter(parameterAccess.getKey(), (ACAQDynamicParameterCollection) parameterHolder));
                labelPanel.add(removeButton, BorderLayout.WEST);
            }

            if (ui.isUILabelEnabled() || parameterHolder instanceof ACAQDynamicParameterCollection)
                addToForm(ui, labelPanel, generateParameterDocumentation(parameterAccess));
            else
                addToForm(ui, generateParameterDocumentation(parameterAccess));
        }
    }

    private MarkdownDocument generateParameterDocumentation(ACAQParameterAccess access) {
        StringBuilder markdownString = new StringBuilder();
        markdownString.append("# Parameter '").append(access.getName()).append("'\n\n");
        ACAQDocumentation documentation = ACAQUIParametertypeRegistry.getInstance().getDocumentationFor(access.getFieldClass());
        if (documentation != null) {
            markdownString.append("<table><tr>");
            markdownString.append("<td><img src=\"").append(ResourceUtils.getPluginResource("icons/wrench.png")).append("\" /></td>");
            markdownString.append("<td><strong>").append(HtmlEscapers.htmlEscaper().escape(documentation.name())).append("</strong>: ");
            markdownString.append(HtmlEscapers.htmlEscaper().escape(documentation.description())).append("</td></tr></table>\n\n");
        }
        if (access.getDescription() != null && !access.getDescription().isEmpty()) {
            markdownString.append(access.getDescription());
        } else {
            markdownString.append("No description provided.");
        }
        return new MarkdownDocument(markdownString.toString());
    }

    private void removeDynamicParameter(String key, ACAQDynamicParameterCollection parameterHolder) {
        ACAQMutableParameterAccess parameter = parameterHolder.getParameter(key);
        if (JOptionPane.showConfirmDialog(this, "Do you really want to remove the parameter '" + parameter.getName() + "'?",
                "Remove parameter", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            parameterHolder.removeParameter(key);
            reloadForm();
        }
    }

    private void initializeAddDynamicParameterButton(JButton addButton, ACAQDynamicParameterCollection parameterHolder) {
        JPopupMenu menu = UIUtils.addPopupMenuToComponent(addButton);
        for (Class<?> allowedType : parameterHolder.getAllowedTypes()) {
            ACAQDocumentation documentation = ACAQUIParametertypeRegistry.getInstance().getDocumentationFor(allowedType);
            String name = allowedType.getSimpleName();
            String description = "Inserts a new parameter";
            if (documentation != null) {
                name = documentation.name();
                description = documentation.description();
            }

            JMenuItem addItem = new JMenuItem(name, UIUtils.getIconFromResources("add.png"));
            addItem.setToolTipText(description);
            addItem.addActionListener(e -> addDynamicParameter(parameterHolder, allowedType));
            menu.add(addItem);
        }
    }

    private void addDynamicParameter(ACAQDynamicParameterCollection parameterHolder, Class<?> fieldType) {
        String name = UIUtils.getUniqueStringByDialog(this, "Please set the parameter name: ", fieldType.getSimpleName(),
                s -> parameterHolder.getParameters().values().stream().anyMatch(p -> Objects.equals(p.getName(), s)));
        if (name != null) {
            ACAQMutableParameterAccess parameterAccess = parameterHolder.addParameter(name, fieldType);
            parameterAccess.setName(name);
            reloadForm();
        }
    }

    /**
     * Triggered when the parameter structure was changed
     *
     * @param event generated event
     */
    @Subscribe
    public void onParameterStructureChanged(ParameterStructureChangedEvent event) {
        reloadForm();
    }

    /**
     * @return The parameterized object
     */
    public ACAQParameterCollection getParameterCollection() {
        return parameterCollection;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
        this.context.inject(this);
    }

    @Override
    public Context context() {
        return context;
    }

    private static List<String> getParameterKeysSortedByParameterName(Map<String, ACAQParameterAccess> parameters, Collection<String> keys) {
        return keys.stream().sorted(Comparator.comparing(k0 -> parameters.get(k0).getName())).collect(Collectors.toList());
    }
}