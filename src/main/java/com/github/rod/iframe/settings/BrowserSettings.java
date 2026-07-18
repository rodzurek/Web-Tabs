package com.github.rod.iframe.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(name = "EmbeddedBrowserSettings", storages = @Storage("embeddedBrowser.xml"))
public final class BrowserSettings implements PersistentStateComponent<BrowserSettings.State> {
    public static final String DEFAULT_URL = "https://www.jetbrains.com";

    public static final class State {
        // Kept for migration from versions before 0.2.0.
        public String url = DEFAULT_URL;
        public List<Page> pages = new ArrayList<>();
    }

    public static final class Page {
        public String name = "";
        public String url = "";

        public Page() {
        }

        public Page(@NotNull String name, @NotNull String url) {
            this.name = name;
            this.url = url;
        }

        public Page(@NotNull Page page) {
            this(page.name, page.url);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Page page
                    && Objects.equals(name, page.name)
                    && Objects.equals(url, page.url);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, url);
        }
    }

    private State state = new State();

    public static BrowserSettings getInstance() {
        return ApplicationManager.getApplication().getService(BrowserSettings.class);
    }

    public @NotNull List<Page> getPages() {
        if (state.pages == null || state.pages.isEmpty()) {
            String migratedUrl = state.url == null || state.url.isBlank() ? DEFAULT_URL : state.url;
            return List.of(new Page("Browser", migratedUrl));
        }
        return state.pages.stream().map(Page::new).toList();
    }

    public void setPages(@NotNull List<Page> pages) {
        state.pages = pages.stream()
                .map(page -> new Page(page.name.trim(), page.url.trim()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        state.url = null;
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
