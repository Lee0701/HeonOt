package io.github.lee0701.heonot.android.inputmethod.modules.global

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.github.lee0701.heonot.android.R
import io.github.lee0701.heonot.android.inputmethod.modules.AndroidSettingsViewCreatable
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter

class ShortcutProcessor : io.github.lee0701.heonot.inputmethod.modules.global.ShortcutProcessor(), AndroidSettingsViewCreatable {

	override fun createSettingsView(context: Context): View {
		val settingsView = LinearLayout(context)
		settingsView.orientation = LinearLayout.VERTICAL

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

}