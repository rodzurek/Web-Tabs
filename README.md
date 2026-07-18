# Web Tabs IntelliJ plugin

An IntelliJ Platform plugin that displays configurable web pages as tabs in a tool window using JCEF (the IDE's embedded Chromium browser).

## Run locally

1. Install JDK 21 and set `JAVA_HOME` to it.
2. Run `./gradlew runIde` (`gradlew.bat runIde` on Windows). The included wrapper downloads Gradle automatically.
3. In the sandbox IDE, open **Settings → Tools → Web Tabs** and add one or more named `http://` or `https://` URLs.
4. Open **View → Tool Windows → Web Tabs**.

After changing the pages, click **Reload tabs** in the tool window. JCEF requires the JetBrains Runtime bundled with normal IntelliJ IDEA installations.

## Build

Run `./gradlew buildPlugin`. The installable ZIP is written to `build/distributions`.
