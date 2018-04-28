package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard

import io.github.lee0701.heonot.*
import io.github.lee0701.heonot.inputmethod.event.*
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.basic.BasicHardKeyboardMap
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import io.github.lee0701.heonot.inputmethod.scripting.nodes.ConstantTreeNode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class BasicHardKeyboard : HardKeyboard() {

	var layout = hashMapOf<Int, BasicHardKeyboardMap>()
	var labels = hashMapOf<Int, Pair<String, String>>()

	var shiftState = false
	var shiftPressing = false
	var altState = false
	var altPressing = false
	var capsLock = false
	var shiftInput = false

	var softLongPressMode = 0
	var labelMode = 0

	var automataState = 0
	var cho = 0
	var jung = 0
	var jong = 0

	@Subscribe
	override fun input(event: HardKeyEvent) {
		if(event.action == HardKeyEvent.HardKeyAction.RELEASE) {
			when(event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftState = false
					updateLabels()
				}
				KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT -> {
					altState = false
					updateLabels()
				}
			}

			when(event.keyCode) {
				KEYCODE_SPACE, KEYCODE_ENTER, KEYCODE_DEL -> updateLabels()
			}
			return
		}

		when(event.keyCode) {
			KEYCODE_DEL -> {
				EventBus.getDefault().post(BackspaceEvent())
				return
			}
			KEYCODE_SPACE -> {
				EventBus.getDefault().post(SpecialKeyEvent(KEYCODE_SPACE))
				return
			}
			KEYCODE_ENTER -> {
				EventBus.getDefault().post(SpecialKeyEvent(KEYCODE_ENTER))
				return
			}
			KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
				shiftState = true
				updateLabels()
				return
			}
			KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT -> {
				altState = true
				updateLabels()
				return
			}
		}

		val node = layout[event.keyCode]?.let {if(shiftState) it.shift else it.normal} ?: ConstantTreeNode(0)
		val evaluator = HeonOt.INSTANCE?.treeEvaluator
		evaluator?.let {
			evaluator.variables = getVariables()
			val result = evaluator.eval(node)
			EventBus.getDefault().post(InputCharEvent(result))
			//Handler().post {
				updateLabels()
			//}
			return@input
		}
		HeonOt.INSTANCE?.directInput(event.keyCode, shiftState, altState)
		updateLabels()
	}

	@Subscribe
	fun onSoftKey(event: SoftKeyEvent) {
		if(event.action == SoftKeyEvent.SoftKeyAction.PRESS) {
			when (event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftPressing = true
					if(event.type == SoftKeyEvent.SoftKeyPressType.SINGLE) {
						if (!shiftState) {
							shiftState = true
						} else {
							if (!capsLock) {
								if (shiftInput) shiftState = false
								else capsLock = true
							} else {
								capsLock = false
								shiftState = false
							}
						}
						shiftInput = false
						updateLabels()
					}
				}
				KEYCODE_DEL -> {
					EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
					EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
				}
				else -> {
					if (softLongPressMode == LONG_PRESS_SHIFT) {
						if (event.type == SoftKeyEvent.SoftKeyPressType.LONG) {
							shiftState = true
							shiftInput = false
							updateLabels()
						}
					} else if (softLongPressMode == LONG_PRESS_REPEAT) {
						EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
					}
				}
			}
		} else if(event.action == SoftKeyEvent.SoftKeyAction.RELEASE) {
			when(event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftPressing = false
					if(shiftInput) {
						capsLock = false
						shiftState = false
					}
					shiftInput = false
					updateLabels()
				}
				KEYCODE_DEL -> {
					updateLabels()
				}
				else -> {
					if (softLongPressMode == LONG_PRESS_SHIFT) {
						EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
						EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
					} else if (softLongPressMode == LONG_PRESS_REPEAT) {
						EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
					}
					//Handler().post {
						if(shiftState) shiftInput = true
						if (!capsLock && shiftInput && !shiftPressing) shiftState = false
						updateLabels()
					//}
				}
			}
		}
	}

	@Subscribe
	fun onAutomataStateChange(event: AutomataStateChangeEvent) {
		automataState = event.state
	}

	@Subscribe
	fun onComposeChar(event: ComposeCharEvent) {
		cho = event.cho
		jung = event.jung
		jong = event.jong
	}

	override fun init() {
		updateLabels()
	}

	override fun pause() {
	}

	override fun clone(): BasicHardKeyboard {
		val cloned = BasicHardKeyboard()
		for(i in this.layout) {
			cloned.layout[i.key] = i.value
		}
		cloned.name = name.toString()
		return cloned
	}

	override fun setProperty(key: String, value: Any) {
		when(key) {
			"layout" -> {
				try {
					if(value is HashMap<*, *>) {
						layout = value as HashMap<Int, BasicHardKeyboardMap>
					} else if(value is JSONObject) {
						this.layout = loadLayout(value)
					}
				} catch(ex: Exception) {
					ex.printStackTrace()
				}
			}
			"soft-long-press-mode" -> {
				if(value is Int) softLongPressMode = value
			}
			"label-mode" -> {
				if(value is Int) labelMode = value
			}
			"labels" -> {
				try {
					if(value is HashMap<*, *>) {
						labels = value as HashMap<Int, Pair<String, String>>
					} else if(value is JSONObject) {
						this.labels = loadLabels(value)
					}
				} catch(ex: Exception) {
					ex.printStackTrace()
				}
			}
			else -> super.setProperty(key, value)
		}
	}

	private fun getVariables() : Map<String, Long> {
		return hashMapOf(
				"T" to automataState.toLong(),
				"P" to if(capsLock) 1L else 0L,
				"D" to cho.toLong(), "E" to jung.toLong(), "F" to jong.toLong()
		)
	}

	private fun getLabels(mode: Int) : Map<Int, String> {
		val result = hashMapOf<Int, String>()
		val evaluator = HeonOt.INSTANCE?.treeEvaluator
		evaluator?.let {
			when(mode) {
				LABEL_FROM_TABLE -> {
					for(i in labels) {
						result += i.key to if(shiftState) i.value.second else i.value.first
					}
				}
				LABEL_CALCULATED -> {
					for(i in layout) {
						evaluator.variables = getVariables()
						result += i.value.keyCode to String(Character.toChars(evaluator.eval(if(shiftState) i.value.shift else i.value.normal).toInt() and 0xffffff))
					}
				}
			}
		}
		return result
	}

	private fun updateLabels() {
		EventBus.getDefault().post(SetPropertyEvent("soft-key-labels", getLabels(labelMode)))
		EventBus.getDefault().post(UpdateStateEvent(UpdateStateEvent.Target.SOFT_KEYBOARD))
	}

	override fun toJSONObject(): JSONObject {
		val obj = super.toJSONObject()
		val properties = JSONObject()

		properties.put("layout", storeLayout())
		properties.put("soft-long-press-mode", softLongPressMode)
		properties.put("label-mode", labelMode)
		properties.put("labels", storeLabels())

		obj.put("properties", properties)
		return obj
	}

	private fun storeLayout() : JSONObject {
		val obj = JSONObject()
		val layout = JSONArray()

		val exporter = StringTreeExporter()

		for(i in this.layout) {
			val entry = JSONObject()
			entry.put("keycode", i.value.keyCode)
			entry.put("normal", exporter.export(i.value.normal))
			entry.put("shift", exporter.export(i.value.shift))
			layout.put(entry)
		}
		obj.put("layout", layout)
		return obj
	}

	private fun storeLabels() : JSONObject {
		val obj = JSONObject()
		val labels = JSONArray()

		for(i in this.labels) {
			val entry = JSONObject()
			entry.put("keycode", i.key)
			entry.put("normal", i.value.first)
			entry.put("shift", i.value.second)
			labels.put(entry)
		}
		obj.put("labels", labels)
		return obj
	}

	companion object {

		val LONG_PRESS_SHIFT = 0
		val LONG_PRESS_REPEAT = 1

		val LABEL_NONE = 0
		val LABEL_FROM_TABLE = 1
		val LABEL_CALCULATED = 2

		fun loadLayout(layoutObject: JSONObject) : HashMap<Int, BasicHardKeyboardMap> {
			val parser = StringRecursionTreeParser()

			val result = hashMapOf<Int, BasicHardKeyboardMap>()
			val table = layoutObject.getJSONArray("layout")

			table?.let {
				for(i in 0 until it.length()) {
					val o = it.getJSONObject(i)

					val keyCode = o.getInt("keycode")
					val normal = parser.parse(o.getString("normal"))
					val shift = parser.parse(o.getString("shift"))

					result += keyCode to BasicHardKeyboardMap(keyCode, normal, shift)
				}
			}
			return result
		}

		fun loadLabels(labelsObject: JSONObject) : HashMap<Int, Pair<String, String>> {
			val result = hashMapOf<Int, Pair<String, String>>()
			val table = labelsObject.getJSONArray("labels")

			table?.let {
				for(i in 0 until it.length()) {
					val o = it.getJSONObject(i)

					val keyCode = o.getInt("keycode")
					val normal = o.getString("normal")
					val shift = o.getString("shift")

					result += keyCode to Pair(normal, shift)

				}
			}

			return result
		}

	}
}
