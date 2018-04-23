package io.github.lee0701.heonot.inputmethod.modules.global

import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.inputmethod.event.HardwareChangeEvent
import io.github.lee0701.heonot.inputmethod.event.InputTypeEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject

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
		val variables = hashMapOf("T" to inputType.toLong())
		node?.let {
			HeonOt.getInstance().treeEvaluator.variables = variables
			val result = HeonOt.getInstance().treeEvaluator.eval(it)
			if(result >= 0) {
				previousInputMethodId = HeonOt.getInstance().currentInputMethodId
				HeonOt.getInstance().changeInputMethod(result.toInt())
			} else if(previousInputMethodId >= 0) {
				HeonOt.getInstance().changeInputMethod(previousInputMethodId)
				previousInputMethodId = -1
			}
		}
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