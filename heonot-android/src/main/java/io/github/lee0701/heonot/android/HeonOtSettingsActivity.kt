package io.github.lee0701.heonot.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import io.github.lee0701.heonot.android.R
import io.github.lee0701.heonot.inputmethod.InputMethod

class HeonOtSettingsActivity : SettingsActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val toolbar = findViewById(R.id.toolbar) as Toolbar
		setSupportActionBar(toolbar)

	}

	override fun onResume() {
		super.onResume()
		loadMethods()
	}

	override fun loadMethods() {
		super.loadMethods()
		setContentView(R.layout.activity_heonot_settings)

		val listView = findViewById(R.id.method_list) as ListView

		val adapter = ListViewAdapter(this, inputMethods)
		listView.adapter = adapter

		listView.setOnItemClickListener { parent, view, position, id ->
			val intent = Intent(this@HeonOtSettingsActivity, InputMethodSettingsActivity::class.java)
			intent.putExtra(SettingsActivity.EXTRA_METHOD_ID, position)
			startActivity(intent)
		}

		val global = findViewById(R.id.global_modules) as TextView
		global.setOnClickListener { v ->
			val intent = Intent(this@HeonOtSettingsActivity, InputMethodSettingsActivity::class.java)
			intent.putExtra(SettingsActivity.EXTRA_METHOD_ID, -1)
			startActivity(intent)
		}

	}

	private class ListViewAdapter internal constructor(context: Context, private val data: List<InputMethod>) : BaseAdapter() {

		private val inflater: LayoutInflater

		init {
			this.inflater = LayoutInflater.from(context)
		}

		override fun getCount(): Int {
			return data.size
		}

		override fun getItem(position: Int): Any {
			return data[position].name
		}

		override fun getItemId(position: Int): Long {
			return position.toLong()
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
			var convertView = convertView
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item, parent, false)
			}
			val method = data[position]
			val text = convertView!!.findViewById(R.id.text) as TextView
			text.text = position.toString() + ": " + method.name

			return convertView
		}
	}

}
