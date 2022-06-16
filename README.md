# LSP-IDEA

A Language Server Protocol client implementation for JetBrains IDEs.


## Bundled Servers

Some language servers require or benefit from special support by clients.
LSP-IDEA comes bundled with extensive rust-analyzer integration
and makes it easy for developers to implement new integrations
on top of the existing infrastructure (see extension points).

### rust-analyzer

- One-click installation of rust-analyzer executable (on Windows)
- Complete configuration UI
- Support for one-click running code through code lenses (e.g. green "Run" arrow next to main method)
- rust-analyzer-specific extensions to LSP

### other

- Use any executable


## Supported Features

- Semantic Tokens-based syntax highlighting
- Code Lenses
- Go to definition/declaration
- Code completion
- Formatting
- Find references
- Highlight usages
- Rename
- Extract variable/constant/function
- Expand/contract selection ((Shift+)Ctrl+W)
- Structure View (Ctrl+F12)
- Go to class/go to symbol
- Code Folding
- Diagnostics
- Quickfixes
- Progress reporting
- Move Item up/down
- On Enter
- Join Lines


## Debugger

One of LSP-IDEA's design goals is debuggability.
To that end,
it features multiple tool windows for inspecting
- JSON-RPC messages, including contents and stacktraces;
- the language server's stderr;
- semantic tokens as decoded by LSP-IDEA, including highlighting selected tokens.
