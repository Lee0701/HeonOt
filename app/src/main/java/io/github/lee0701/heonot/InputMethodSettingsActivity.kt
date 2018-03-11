package io.github.lee0701.heonot

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import io.github.lee0701.heonot.inputmethod.InputMethod
import kotlinx.android.synthetic.main.activity_input_method_settings.*
import kotlinx.android.synthetic.main.settings_toolbar.*
import org.json.JSONException


class InputMethodSettingsActivity : SettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_method_settings)

        val id = intent.getIntExtra(SettingsActivity.EXTRA_METHOD_ID, -1)
        val method = inputMethods[id]

        modules.orientation = LinearLayout.VERTICAL

        for (module in method.modules) {
            modules.addView(module.createSettingsView(this))
        }

        val buttonsLayout = LinearLayout(this)

        val exportButton = Button(this)
        exportButton.setText(R.string.button_export)
        exportButton.setOnClickListener { v ->
            val json: String
            try {
                json = method.toJSON(2)
            } catch (e: JSONException) {
                return@setOnClickListener
            }

            AlertDialog.Builder(this)
                .setMessage(R.string.msg_copy_method_json)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    if (Build.VERSION.SDK_INT >= 11) {
                        val clipData = ClipData.newPlainText("JSON Data", json)
                        clipboard.primaryClip = clipData
                    } else {
                        clipboard.text = json
                    }
                }
                .setNegativeButton(R.string.button_cancel, { _, _ -> })
                .create()
                .show()
        }
        buttonsLayout.addView(exportButton)

        val duplicateButton = Button(this)
        duplicateButton.setText(R.string.button_duplicate)
        duplicateButton.setOnClickListener({ v ->
            inputMethods.add(InputMethod(method))
            saveMethods()
            finish()
        })
        buttonsLayout.addView(duplicateButton)

        val deleteButton = Button(this)
        deleteButton.setText(R.string.button_delete)
        deleteButton.setOnClickListener { _ ->
            inputMethods.remove(method)
            saveMethods()
            finish()
        }

        buttonsLayout.addView(deleteButton)

        modules.addView(buttonsLayout)

        setSupportActionBar(toolbar)

    }
}
