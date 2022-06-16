package info.voidev.lspidea.features.gotosymbol

import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.navigation.ChooseByNameContributorEx2
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter
import info.voidev.lspidea.symbol.LspSymbolProvider
import org.eclipse.lsp4j.SymbolKind
import java.util.EnumSet

open class LspChooseByNameContributor(
    private val filterByKind: Set<SymbolKind> = EnumSet.allOf(SymbolKind::class.java)
) : ChooseByNameContributorEx2 {

    override fun processNames(processor: Processor<in String>, parameters: FindSymbolParameters) {
        //TODO or localPatternName?
        LspSymbolProvider.get(parameters.completePattern, parameters.project)
            .filter { it.info.kind in filterByKind }
            .filter { it.file?.let(parameters.searchScope::contains) == true }
            .map { it.info.name }
            .forEach(processor::process)
    }

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        LspSymbolProvider.get(scope.project?.getUserData(ChooseByNamePopup.CURRENT_SEARCH_PATTERN), scope.project)
            .filter { it.info.kind in filterByKind }
            .filter { it.file?.let(scope::contains) == true }
            .map { it.info.name }
            .forEach(processor::process)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        //TODO or localPatternName?
        LspSymbolProvider.get(parameters.completePattern, parameters.project)
            .filter { it.info.kind in filterByKind }
            .filter { it.file?.let(parameters.searchScope::contains) == true }
            .distinct()
            .forEach(processor::process)
    }
}

class LspGoToClassContributor : LspChooseByNameContributor(EnumSet.of(
    SymbolKind.Class,
    SymbolKind.Enum,
    SymbolKind.Interface,
    SymbolKind.Struct,
))

class LspGoToSymbolContributor : LspChooseByNameContributor(EnumSet.of(
    SymbolKind.Class,
    SymbolKind.Enum,
    SymbolKind.Interface,
    SymbolKind.Struct,

    SymbolKind.Method,
    SymbolKind.Property,
    SymbolKind.Field,
    SymbolKind.Function,
    SymbolKind.Constant,
    SymbolKind.EnumMember,
    SymbolKind.Operator,
))

class LspGoToFileContributor : LspChooseByNameContributor(EnumSet.of(
    SymbolKind.File
))
