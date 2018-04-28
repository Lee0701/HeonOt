package io.github.lee0701.heonot.android.inputmethod.modules.generator

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.github.lee0701.heonot.android.R
import io.github.lee0701.heonot.android.inputmethod.modules.AndroidSettingsViewCreatable
import io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler
import java.util.*

class UnicodeCharacterGenerator : io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeCharacterGenerator(), AndroidSettingsViewCreatable {

	override fun createSettingsView(context: Context): View {
		val settings = LinearLayout(context)
		settings.orientation = LinearLayout.VERTICAL

		val moajugi = CheckBox(context)
		moajugi.setText(R.string.moajugi)
		moajugi.isChecked = this.moajugi
		moajugi.setOnCheckedChangeListener { v, checked -> this.moajugi = checked }
		settings.addView(moajugi)

		val fullMoachigi = CheckBox(context)
		fullMoachigi.setText(R.string.full_moachigi)
		fullMoachigi.isChecked = this.fullMoachigi
		fullMoachigi.setOnCheckedChangeListener { v, checked -> this.fullMoachigi = checked }
		settings.addView(fullMoachigi)

		val firstMidEnd = CheckBox(context)
		firstMidEnd.setText(R.string.first_mid_end)
		firstMidEnd.isChecked = this.firstMidEnd
		firstMidEnd.setOnCheckedChangeListener { v, checked -> this.firstMidEnd = checked }
		settings.addView(firstMidEnd)

		val recyclerView = RecyclerView(context)
		val adapter = CombinationTableAdapter(context)
		recyclerView.adapter = adapter
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.isNestedScrollingEnabled = false

		val callback = object : ItemTouchHelper.SimpleCallback(0,
				ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val index = Collections.binarySearch<UnicodeJamoHandler.JamoPair>(combinationTable, (viewHolder as CombinationTableViewHolder).jamoPair)
				if (index < 0) return
				combinationTable.removeAt(index)
				sortCombinationTable()
				adapter.notifyDataSetChanged()
			}
		}

		val title = LinearLayout(context)

		val titleText = TextView(context)
		titleText.setText(R.string.combination_table)
		titleText.setTextSize(TypedValue.COMPLEX_UNIT_PT, 11f)
		title.addView(titleText)

		val addButton = Button(context)
		addButton.setText(R.string.button_add)
		addButton.setOnClickListener { v ->
			val view = LayoutInflater.from(context).inflate(R.layout.combination_table_edit, null) as ViewGroup
			val left = view.findViewById(R.id.combination_left) as EditText
			val right = view.findViewById(R.id.combination_right) as EditText
			val result = view.findViewById(R.id.combination_result) as EditText
			AlertDialog.Builder(context)
					.setView(view)
					.setPositiveButton(R.string.button_ok) { dialog, which ->
						try {
							val pair = UnicodeJamoHandler.JamoPair(UnicodeJamoHandler.parseCharCode(left.text.toString()).toChar(), UnicodeJamoHandler.parseCharCode(right.text.toString()).toChar())
							val index = Collections.binarySearch<UnicodeJamoHandler.JamoPair>(combinationTable, pair)
							if (index >= 0) {
								Toast.makeText(context, R.string.msg_same_value_overwritten, Toast.LENGTH_SHORT).show()
								combinationTable.removeAt(index)
							}
							val resultChar = UnicodeJamoHandler.parseCharCode(result.text.toString()).toChar()
							combinationTable.add(Combination(pair, resultChar))
							sortCombinationTable()
							adapter.notifyDataSetChanged()
						} catch (e: NumberFormatException) {
							Toast.makeText(context, R.string.msg_illegal_number_format, Toast.LENGTH_SHORT).show()
						}
					}
					.setNegativeButton(R.string.button_cancel) { dialog, which -> }
					.create().show()
		}
		title.addView(addButton)

		settings.addView(title)

		val itemTouchHelper = ItemTouchHelper(callback)
		itemTouchHelper.attachToRecyclerView(recyclerView)
		settings.addView(recyclerView)

		return settings
	}

	internal inner class CombinationTableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		var combinationLeft: TextView
		var combinationRight: TextView
		var combinationResult: TextView
		var jamoPair: UnicodeJamoHandler.JamoPair? = null

		init {
			combinationLeft = itemView.findViewById(R.id.combination_left_text) as TextView
			combinationRight = itemView.findViewById(R.id.combination_right_text) as TextView
			combinationResult = itemView.findViewById(R.id.combination_result_text) as TextView
		}

		@SuppressLint("SetTextI18n")
		fun onBind(jamoPair: UnicodeJamoHandler.JamoPair, character: Char) {
			this.jamoPair = jamoPair
			combinationLeft.text = jamoPair.a + " (" + UnicodeJamoHandler.getType(jamoPair.a).name + ")"
			combinationRight.text = jamoPair.b + " (" + UnicodeJamoHandler.getType(jamoPair.b).name + ")"
			combinationResult.text = character + " (" + UnicodeJamoHandler.getType(character).name + ")"
		}
	}

	internal inner class CombinationTableAdapter(private val context: Context) : RecyclerView.Adapter<CombinationTableViewHolder>() {
		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombinationTableViewHolder {
			val combinationTableView = LayoutInflater
					.from(context)
					.inflate(R.layout.combination_table_view_holder, parent, false)
			return CombinationTableViewHolder(combinationTableView)
		}

		override fun onBindViewHolder(holder: CombinationTableViewHolder, position: Int) {
			val combination = combinationTable[position]
			holder.onBind(combination.source, combination.result)
		}

		override fun getItemCount(): Int {
			return combinationTable.size
		}
	}

}
