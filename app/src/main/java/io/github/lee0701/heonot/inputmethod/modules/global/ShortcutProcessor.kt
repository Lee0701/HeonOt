package io.github.lee0701.heonot.inputmethod.modules.global

import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.KeyStroke
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class ShortcutProcessor : InputMethodModule() {

    var shortcuts: List<Shortcut> = arrayListOf()

    override fun init() {
        // do nothing
    }

    override fun pause() {
        // do nothing
    }

    @Subscribe(priority = 1)
    fun onHardKey(event: HardKeyEvent) {
        if (event.action === HardKeyEvent.HardKeyAction.PRESS && processShortcut(
                event.keyCode,
                event.isAltPressed,
                event.isShiftPressed
            )
        ) {
            EventBus.getDefault().cancelEventDelivery(event)
        }
    }

    fun processShortcut(keyCode: Int, altPressed: Boolean, shiftPressed: Boolean): Boolean {
        val evaluator = HeonOt.getInstance().treeEvaluator
        for (shortcut in shortcuts) {
            val keyStroke = shortcut.keyStroke
            if (keyStroke.keyCode == keyCode
                && keyStroke.isAlt == altPressed
                && keyStroke.isShift == shiftPressed
            ) {
                evaluator.variables = HeonOt.getInstance().variables
                val result = evaluator.eval(shortcut.treeNode)
                when (shortcut.mode) {
                    Shortcut.MODE_CHANGE -> HeonOt.getInstance().changeInputMethod(result.toInt())
                    Shortcut.MODE_INPUT -> EventBus.getDefault().post(CommitCharEvent(result.toChar(), 1))
                }
                return true
            }
        }
        return false
    }

    override fun toJSONObject(): JSONObject {
        val obj = super.toJSONObject()
        val properties = JSONObject()

        properties.put("shortcuts", storeShortcuts())

        obj.put("properties", properties)
        return obj
    }

    override fun setProperty(key: String?, value: Any?) {
        when(key) {
            "shortcuts" -> {
                val parser = StringRecursionTreeParser()
                if(value is JSONObject) {
                    val shortcuts = value.getJSONArray("shortcuts")
                    for(i in 0 until shortcuts.length()) {
                        val o = shortcuts.getJSONObject(i)
                        val keyStroke = KeyStroke.fromJsonObject(o.getJSONObject("keystroke"))
                        val mode = o.getInt("mode")
                        val node = parser.parse(o.getString("node"))
                        this.shortcuts += Shortcut(keyStroke, mode, node)
                    }
                }
            }
        }
    }

    private fun storeShortcuts(): JSONObject {
        val obj = JSONObject()
        val shortcuts = JSONArray()
        val exporter = StringTreeExporter()
        for(i in this.shortcuts) {
            val shortcut = JSONObject()
            shortcut.put("keystroke", i.keyStroke.toJsonObject())
            shortcut.put("mode", i.mode)
            shortcut.put("node", exporter.export(i.treeNode))
            shortcuts.put(shortcut)
        }
        obj.put("shortcuts", shortcuts)
        return obj
    }

    override fun clone(): ShortcutProcessor {
        var cloned = ShortcutProcessor()
        for(shortcut in shortcuts) {
            cloned.shortcuts += shortcut.clone()
        }
        return this
    }

    class Shortcut(var keyStroke: KeyStroke, var mode: Int, var treeNode: TreeNode) : Cloneable {
        public override fun clone(): Shortcut {
            return Shortcut(keyStroke.clone(), mode, treeNode.clone())
        }
        companion object {
            @JvmField
            val MODE_NONE = 0
            @JvmField
            val MODE_CHANGE = 1
            @JvmField
            val MODE_INPUT = 2
        }
    }
}
