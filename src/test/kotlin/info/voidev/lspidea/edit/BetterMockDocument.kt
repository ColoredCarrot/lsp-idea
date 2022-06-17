package info.voidev.lspidea.edit

import com.intellij.mock.MockDocument
import com.intellij.util.LocalTimeCounter

class BetterMockDocument : MockDocument() {

    override fun setText(text: CharSequence) {
        replaceText(text, LocalTimeCounter.currentTime())
    }

}
