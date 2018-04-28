package io.github.lee0701.heonot.android.inputmethod.modules.hardkeyboard

import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.text.method.MetaKeyKeyListener
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import io.github.lee0701.heonot.android.R
import io.github.lee0701.heonot.android.inputmethod.modules.AndroidSettingsViewCreatable

import java.util.HashMap

import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.def.DefaultHardKeyboardMap

import io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.parseCharCode

class DefaultHardKeyboard : io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.DefaultHardKeyboard(), AndroidSettingsViewCreatable {

	override fun createSettingsView(context: Context): View {
		val settings = LinearLayout(context)
		settings.orientation = LinearLayout.VERTICAL

		val keyboardView = KeyboardView(context, null)
		val keyboard = Keyboard(context, R.xml.keyboard_full_pc)
		keyboardView.keyboard = keyboard
		keyboardView.isPreviewEnabled = false
		keyboardView.setOnKeyboardActionListener(object : KeyboardView.OnKeyboardActionListener {
			override fun onKey(primaryCode: Int, keyCodes: IntArray) {
				createKeyEditDialog(context, primaryCode).show()
			}

			override fun onPress(primaryCode: Int) {}
			override fun onRelease(primaryCode: Int) {}
			override fun onText(text: CharSequence) {}
			override fun swipeLeft() {}
			override fun swipeRight() {}
			override fun swipeDown() {}
			override fun swipeUp() {}
		})
		settings.addView(keyboardView)
		val repeat = CheckBox(context)
		repeat.setText(R.string.dsk_pref_soft_key_repeat)
		repeat.setOnCheckedChangeListener { v, checked -> softLongPressMode = if (checked) DefaultHardKeyboard.LONG_PRESS_REPEAT else DefaultHardKeyboard.LONG_PRESS_SHIFT }
		settings.addView(repeat)

		return settings
	}

	fun createKeyEditDialog(context: Context, keyCode: Int): AlertDialog {
		val content = LinearLayout(context)
		content.orientation = LinearLayout.VERTICAL
		val normal = EditText(context)
		normal.setHint(R.string.dhk_key_normal)
		normal.ellipsize = TextUtils.TruncateAt.END
		normal.setSingleLine(true)
		val shift = EditText(context)
		shift.setHint(R.string.dhk_key_shifted)
		shift.ellipsize = TextUtils.TruncateAt.END
		shift.setSingleLine(true)
		val map = layout[keyCode] ?: DefaultHardKeyboardMap(keyCode, 0, 0, 0)

		normal.setText("0x" + Integer.toHexString(map.normal))
		shift.setText("0x" + Integer.toHexString(map.shift))
		var til: TextInputLayout
		til = TextInputLayout(context)
		til.addView(normal)
		content.addView(til)
		til = TextInputLayout(context)
		til.addView(shift)
		content.addView(til)
		return AlertDialog.Builder(context)
				.setTitle("Key $keyCode")
				.setView(content)
				.setPositiveButton(R.string.button_ok) { dialog, which ->
					try {
						layout += keyCode to DefaultHardKeyboardMap(keyCode,
								parseCharCode(normal.text.toString()),
								parseCharCode(shift.text.toString()),
								parseCharCode(shift.text.toString()))
					} catch (e: NumberFormatException) {
						Toast.makeText(context, R.string.msg_illegal_number_format, Toast.LENGTH_SHORT).show()
					}
				}
				.setNeutralButton(R.string.button_delete) { dialog, which -> layout -= keyCode }
				.setNegativeButton(R.string.button_cancel) { dialog, which -> }
				.create()
	}

	companion object {
		val LONG_PRESS_SHIFT = 0
		val LONG_PRESS_REPEAT = 1
	}

}
