# Editor integration

Before setting up your editor, see [Project setup](../README.md#project-setup)
on how to configure `clj-kondo` for your project. TL;DR: this involves creating
a `.clj-kondo` directory in the root of your project.

## Emacs

For integrating with Emacs, see
[flycheck-clj-kondo](https://github.com/borkdude/flycheck-clj-kondo).

## IntelliJ IDEA

<img src="../screenshots/intellij-let.png" width="50%" align="right">

This section assumes that you are already using
[Cursive](https://cursive-ide.com).

1. Install the [File
Watchers](https://www.jetbrains.com/help/idea/settings-tools-file-watchers.html)
plugin.

Repeat the below steps for the file types Clojure (`.clj`), ClojureScript (`.cljs`)
and CLJC (`.cljc`).

2. Under Preferences / Tools / File Watchers click `+` and choose the `<custom>`
   template.
3. Choose a name. E.g. `clj-kondo <filetype>` (where `<filetype>` is one of
   Clojure, ClojureScript or CLJC).
4. In the File type field, choose the correct filetype.
5. In the Program field, type `clj-kondo`.
6. In the Arguments field, type `--lint $FilePath$ --cache`.
7. In the Working directory field, type `$FileDir$`.
8. Enable `Create output file from stdout`
9. In output filters put `$FILE_PATH$:$LINE$:$COLUMN$: $MESSAGE$`.

<img src="../screenshots/intellij-fw-config.png" align="right">
