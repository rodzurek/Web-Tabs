package com.github.rod.iframe.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class BrowserConfigurable implements Configurable {
    private DefaultTableModel model;
    private JBTable table;
    private JPanel panel;

    @Override
    public @Nls String getDisplayName() {
        return "Web Tabs";
    }

    @Override
    public @Nullable JComponent createComponent() {
        model = new DefaultTableModel(new Object[]{"Tab name", "URL"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        table = new JBTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);
        table.getColumnModel().getColumn(1).setPreferredWidth(500);

        JButton add = new JButton("Add");
        add.addActionListener(event -> {
            model.addRow(new Object[]{"New tab", "https://"});
            int row = model.getRowCount() - 1;
            table.setRowSelectionInterval(row, row);
            table.editCellAt(row, 0);
        });

        JButton remove = new JButton("Remove");
        remove.addActionListener(event -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                model.removeRow(row);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(add);
        buttons.add(remove);

        panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JBLabel("Configure one page per browser tab. URLs must begin with http:// or https://."), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        return model != null && !readPages().equals(BrowserSettings.getInstance().getPages());
    }

    @Override
    public void apply() throws ConfigurationException {
        stopEditing();
        List<BrowserSettings.Page> pages = readPages();
        if (pages.isEmpty()) {
            throw new ConfigurationException("Add at least one browser tab.", "No browser tabs");
        }
        for (int index = 0; index < pages.size(); index++) {
            BrowserSettings.Page page = pages.get(index);
            if (page.name.isBlank()) {
                throw new ConfigurationException("Enter a name for tab " + (index + 1) + ".", "Missing tab name");
            }
            if (!isValidWebUrl(page.url)) {
                throw new ConfigurationException(
                        "Enter a valid http:// or https:// URL for tab '" + page.name + "'.",
                        "Invalid URL"
                );
            }
        }
        BrowserSettings.getInstance().setPages(pages);
    }

    @Override
    public void reset() {
        if (model != null) {
            model.setRowCount(0);
            for (BrowserSettings.Page page : BrowserSettings.getInstance().getPages()) {
                model.addRow(new Object[]{page.name, page.url});
            }
        }
    }

    @Override
    public void disposeUIResources() {
        model = null;
        table = null;
        panel = null;
    }

    private List<BrowserSettings.Page> readPages() {
        stopEditing();
        List<BrowserSettings.Page> pages = new ArrayList<>();
        for (int row = 0; row < model.getRowCount(); row++) {
            pages.add(new BrowserSettings.Page(
                    String.valueOf(model.getValueAt(row, 0)).trim(),
                    String.valueOf(model.getValueAt(row, 1)).trim()
            ));
        }
        return pages;
    }

    private void stopEditing() {
        if (table != null && table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private static boolean isValidWebUrl(String value) {
        try {
            URI uri = URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
