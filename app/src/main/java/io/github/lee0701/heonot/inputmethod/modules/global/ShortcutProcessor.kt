package io.github.lee0701.heonot.inputmethod.modules.global

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.R
import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.KeyStroke
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import io.github.lee0701.heonot.inputmethod.scripting.TreeExporter
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

    override fun createSettingsView(context: Context): View {
        val settingsView = LinearLayout(context)
        settingsView.orientation = LinearLayout.VERTICAL
        settingsView.addView(super.createSettingsView(context))

        val shortcutsView = RecyclerView(context)
        shortcutsView.adapter = ShortcutAdapter(context)
        shortcutsView.layoutManager = LinearLayoutManager(context)
        shortcutsView.isNestedScrollingEnabled = false

        settingsView.addView(shortcutsView)

        return settingsView
    }

    inner class ShortcutAdapter : RecyclerView.Adapter<ShortcutViewHolder> {

        private val context: Context
        private val exporter = StringTreeExporter()

        constructor(context: Context) : super() {
            this.context = context
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ShortcutViewHolder {
            val v = LayoutInflater.from(context).inflate(R.layout.shortcut_view_holder, parent, false)
            return ShortcutViewHolder(v, exporter)
        }

        override fun getItemCount(): Int {
            return shortcuts.count()
        }

        override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
            val shortcut = shortcuts.get(position)
            holder.onBind(shortcut)
        }
    }

    inner class ShortcutViewHolder : RecyclerView.ViewHolder {

        private val exporter: StringTreeExporter

        private val mode: TextView
        private val stroke: TextView
        private val node: TextView

        constructor(itemView: View, exporter: StringTreeExporter) : super(itemView) {
            this.exporter = exporter
            mode = itemView.findViewById(R.id.shortcut_mode) as TextView
            stroke = itemView.findViewById(R.id.shortcut_stroke) as TextView
            node = itemView.findViewById(R.id.shortcut_node) as TextView
        }

        fun onBind(shortcut: Shortcut) {
            mode.text = shortcut.mode.toString(10)
            stroke.text = shortcut.keyStroke.toString()
            node.text = exporter.export(shortcut.treeNode) as String
        }

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
