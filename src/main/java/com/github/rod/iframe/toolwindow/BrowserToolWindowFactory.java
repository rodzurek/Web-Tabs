package com.github.rod.iframe.toolwindow;

import com.github.rod.iframe.settings.BrowserSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

public final class BrowserToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        if (!JBCefApp.isSupported()) {
            JPanel root = new JPanel(new BorderLayout());
            JBLabel unavailable = new JBLabel(
                    "JCEF is unavailable in this IDE runtime. Run the IDE with the bundled JetBrains Runtime.",
                    JBLabel.CENTER
            );
            root.add(unavailable, BorderLayout.CENTER);
            addContent(toolWindow, root, null);
            return;
        }

        BrowserPanel browserPanel = new BrowserPanel(project);
        addContent(toolWindow, browserPanel, browserPanel);
    }

    private static JPanel createToolbar(Project project, BrowserPanel browserPanel) {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()));

        JButton reload = new JButton("Reload tabs");
        reload.addActionListener(event -> browserPanel.rebuildTabs());

        JButton settings = new JButton("Settings…");
        settings.addActionListener(event -> ShowSettingsUtil.getInstance()
                .showSettingsDialog(project, "Web Tabs"));

        JPanel buttons = new JPanel();
        buttons.add(reload);
        buttons.add(settings);
        toolbar.add(buttons, BorderLayout.WEST);
        return toolbar;
    }

    private static final class BrowserPanel extends JPanel implements Disposable {
        private final JTabbedPane tabs = new JTabbedPane();
        private final List<JBCefBrowser> browsers = new ArrayList<>();

        private BrowserPanel(Project project) {
            super(new BorderLayout());
            add(createToolbar(project, this), BorderLayout.NORTH);
            add(tabs, BorderLayout.CENTER);
            rebuildTabs();
        }

        private void rebuildTabs() {
            disposeBrowsers();
            tabs.removeAll();
            for (BrowserSettings.Page page : BrowserSettings.getInstance().getPages()) {
                JBCefBrowser browser = new JBCefBrowser(page.url);
                browsers.add(browser);
                tabs.addTab(page.name, browser.getComponent());
                tabs.setToolTipTextAt(tabs.getTabCount() - 1, page.url);
            }
        }

        private void disposeBrowsers() {
            for (JBCefBrowser browser : browsers) {
                Disposer.dispose(browser);
            }
            browsers.clear();
        }

        @Override
        public void dispose() {
            disposeBrowsers();
        }
    }

    private static void addContent(ToolWindow toolWindow, JPanel panel, Disposable disposable) {
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        if (disposable != null) {
            Disposer.register(content, disposable);
        }
        toolWindow.getContentManager().addContent(content);
    }
}
