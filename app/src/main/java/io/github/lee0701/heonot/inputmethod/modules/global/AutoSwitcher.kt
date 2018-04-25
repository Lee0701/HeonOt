package io.github.lee0701.heonot.inputmethod.modules.global

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.R
import io.github.lee0701.heonot.inputmethod.event.HardwareChangeEvent
import io.github.lee0701.heonot.inputmethod.event.InputTypeEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import kotlin.math.exp

class AutoSwitcher : InputMethodModule() {

	var node: TreeNode? = null
	var inputType = 0
	var hardwareState = 0
	var previousInputMethodId = -1

	override fun init() {

	}

	override fun pause() {

	}

	override fun setProperty(key: String?, value: Any?) {
		when(key) {
			"node" -> {
				this.node = StringRecursionTreeParser().parse(value)
			}
			else -> super.setProperty(key, value)
		}
	}

	@Subscribe
	fun onInputType(event: InputTypeEvent) {
		inputType = event.inputType
		autoSwitch();
	}

	@Subscribe
	fun onHardwareChange(event: HardwareChangeEvent) {
		hardwareState = if(event.hardwareKeyboardState) 1 else 0
		autoSwitch()
	}

	private fun autoSwitch() {
		val variables = hashMapOf("T" to inputType.toLong(), "H" to hardwareState.toLong())
		node?.let {
			HeonOt.getInstance().treeEvaluator.let { evaluator ->
				evaluator.variables = variables
				val result = evaluator.eval(it)

				if(result >= 0) {
					previousInputMethodId = HeonOt.getInstance().currentInputMethodId
					HeonOt.getInstance().changeInputMethod(result.toInt())
				} else if(previousInputMethodId >= 0) {
					HeonOt.getInstance().changeInputMethod(previousInputMethodId)
					previousInputMethodId = -1
				}
			}
		}
	}

	override fun createSettingsView(context: Context?): View {
		val view = super.createSettingsView(context) as ViewGroup

		val exporter = StringTreeExporter()
		val parser = StringRecursionTreeParser()
		parser.setConstants(mutableMapOf(
				"HARDWARE_ON" to 1.toLong(),
				"HARDWARE_OFF" to 0.toLong(),
				"TYPE_MASK_CLASS" to EditorInfo.TYPE_MASK_CLASS.toLong(),
				"TYPE_MASK_FLAGS" to EditorInfo.TYPE_MASK_FLAGS.toLong(),
				"TYPE_MASK_VARIATION" to EditorInfo.TYPE_MASK_VARIATION.toLong(),
				"TYPE_CLASS_DATETIME" to EditorInfo.TYPE_CLASS_DATETIME.toLong(),
				"TYPE_CLASS_NUMBER" to EditorInfo.TYPE_CLASS_NUMBER.toLong(),
				"TYPE_CLASS_TEXT" to EditorInfo.TYPE_CLASS_TEXT.toLong(),
				"TYPE_CLASS_PHONE" to EditorInfo.TYPE_CLASS_PHONE.toLong(),
				"TYPE_TEXT_VARIATION_PASSWORD" to EditorInfo.TYPE_TEXT_VARIATION_PASSWORD.toLong(),
				"TYPE_TEXT_VARIATION_VISIBLE_PASSWORD" to EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD.toLong(),
				"TYPE_TEXT_VARIATION_WEB_PASSWORD" to EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD.toLong(),
				"TYPE_TEXT_VARIATION_URI" to EditorInfo.TYPE_TEXT_VARIATION_URI.toLong()
				))

		val layout = TextInputLayout(context)
		val expression = TextInputEditText(context)
		expression.setHint(R.string.expression)
		expression.ellipsize = TextUtils.TruncateAt.END
		expression.setSingleLine()
		expression.setText(exporter.export(node) as String)
		expression.imeOptions = EditorInfo.IME_ACTION_DONE
		expression.setOnFocusChangeListener { v, hasFocus ->
			try {
				node = parser.parse(expression.text.toString())
			} catch(ex: Exception) {
				Toast.makeText(context, R.string.msg_expression_parse_failed, Toast.LENGTH_SHORT).show()
			}
		}
		layout.addView(expression)

		view.addView(layout)

		return view
	}

	override fun toJSONObject(): JSONObject {
		val obj = super.toJSONObject()
		val properties = JSONObject()
		node?.let {
			properties.put("node", StringTreeExporter().export(it))
		}
		obj.put("properties", properties)
		return obj
	}

	override fun clone(): AutoSwitcher {
		val cloned = AutoSwitcher()
		cloned.node = node?.clone()
		return cloned
	}

}