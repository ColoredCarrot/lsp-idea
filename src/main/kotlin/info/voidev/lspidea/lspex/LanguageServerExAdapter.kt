package info.voidev.lspidea.lspex

import org.eclipse.lsp4j.services.LanguageServer

class LanguageServerExAdapter(private val delegate: LanguageServer) : LanguageServerEx, LanguageServer by delegate
