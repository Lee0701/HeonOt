package io.github.lee0701.heonot

import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_input_method_settings.*
import kotlinx.android.synthetic.main.settings_toolbar.*

class InputMethodSettingsActivity : SettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_method_settings)

        val method = inputMethods[intent.getIntExtra(SettingsActivity.EXTRA_METHOD_ID, -1)]

        modules.orientation = LinearLayout.VERTICAL

        for (module in method.modules) {
            val textView = TextView(this)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12f)
            textView.text = module.name
            modules.addView(textView)
            modules.addView(module.createSettingsView(this))
        }

        setSupportActionBar(toolbar)
    }
}
