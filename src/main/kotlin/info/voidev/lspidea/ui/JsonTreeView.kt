package info.voidev.lspidea.ui

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation

// TODO: Delete this class or refactor it and make it pretty for some use case
@Deprecated("unused")
class JsonTreeView(private val value: JsonElement) : XStackFrame() {

    override fun computeChildren(node: XCompositeNode) {
        node.addChildren(XValueChildrenList.singleton("<root>", Node(value)), true)
    }

    class Node(private val value: JsonElement?) : XValue() {
        override fun computePresentation(node: XValueNode, place: XValuePlace) {
            node.setPresentation(null, Presentation(value), value?.let { it.isJsonObject || it.isJsonArray } == true)
        }

        override fun computeChildren(node: XCompositeNode) {
            val children = XValueChildrenList()

            when (value) {
                is JsonObject -> {
                    for ((k, v) in value.entrySet()) {
                        children.add(k, Node(v))
                    }
                }
                is JsonArray -> {
                    for ((i, v) in value.withIndex()) {
                        children.add("$i", Node(v))
                    }
                }
            }

            node.addChildren(children, true)
        }

        override fun canNavigateToSource() = false
    }

    private class Presentation(private val value: JsonElement?) : XValuePresentation() {
        override fun getSeparator() = ": "

        override fun getType(): String? = when {
            value == null || value.isJsonNull -> null
            value.isJsonObject -> "object"
            value.isJsonArray -> "array"
            else -> null
        }

        override fun renderValue(renderer: XValueTextRenderer) {
            when (value) {
                is JsonObject, is JsonArray -> {}
                is JsonPrimitive -> when {
                    value.isString -> renderer.renderStringValue(value.asString)
                    value.isNumber -> renderer.renderNumericValue(value.asNumber.toString())
                    value.isBoolean -> renderer.renderKeywordValue(if (value.asBoolean) "true" else "false")
                    else -> thisLogger().assertTrue(false, "JSON primitive that is not a string, number of boolean")
                }
                is JsonNull, null -> renderer.renderKeywordValue("null")
                else -> thisLogger().assertTrue(false)
            }
        }
    }
}
