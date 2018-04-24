package io.github.lee0701.heonot

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
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
        var method = globalModules
        if(id >= 0) method = inputMethods[id]

        for (module in method.modules) {
            modules.addTab(modules.newTab().setText(module.name))
        }

        val listener = object: TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                module_settings.removeAllViews()
                module_settings.addView(method.modules.get(modules.selectedTabPosition).createSettingsView(this@InputMethodSettingsActivity))

            }
        }
        modules.addOnTabSelectedListener(listener)

        val importButton = Button(this@InputMethodSettingsActivity)
        importButton.setText(R.string.button_import)
        importButton.setOnClickListener {
            AlertDialog.Builder(this@InputMethodSettingsActivity)
                    .setMessage(R.string.msg_paste_method_json)
                    .setPositiveButton(R.string.button_ok) { _, _ ->
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val json = run {
                            if (Build.VERSION.SDK_INT >= 11) {
                                val clipData = clipboard.primaryClip
                                clipData.getItemAt(0)?.coerceToText(this@InputMethodSettingsActivity).toString()
                            } else {
                                clipboard.text.toString()
                            }
                        }
                        try {
                            if(json.isNotEmpty()) {
                                val method = InputMethod.loadJSON(json)
                                inputMethods[id] = method
                                saveMethods()
                                finish()
                            } else throw Exception()
                        } catch(ex: Exception) {
                            AlertDialog.Builder(this@InputMethodSettingsActivity)
                                    .setMessage(R.string.msg_error_importing)
                                    .create()
                                    .show()
                        }
                    }
                    .setNegativeButton(R.string.button_cancel, { _, _ -> })
                    .create()
                    .show()
        }
        buttons.addView(importButton)

        val exportButton = Button(this@InputMethodSettingsActivity)
        exportButton.setText(R.string.button_export)
        exportButton.setOnClickListener { _ ->
            val json: String
            try {
                json = method.toJSON(2)
            } catch (e: JSONException) {
                return@setOnClickListener
            }

            AlertDialog.Builder(this@InputMethodSettingsActivity)
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
        buttons.addView(exportButton)

        val duplicateButton = Button(this@InputMethodSettingsActivity)
        duplicateButton.setText(R.string.button_duplicate)
        duplicateButton.setOnClickListener({ _ ->
            inputMethods.add(InputMethod(method))
            saveMethods()
            finish()
        })
        buttons.addView(duplicateButton)

        val deleteButton = Button(this@InputMethodSettingsActivity)
        deleteButton.setText(R.string.button_delete)
        deleteButton.setOnClickListener { _ ->
            inputMethods.remove(method)
            saveMethods()
            finish()
        }
        buttons.addView(deleteButton)

        listener.onTabSelected(modules.getTabAt(0))

        setSupportActionBar(toolbar)

    }
}
