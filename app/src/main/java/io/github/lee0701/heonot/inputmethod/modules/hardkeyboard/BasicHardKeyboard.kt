package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard

import android.os.Handler
import android.view.KeyEvent
import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.inputmethod.event.*
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.basic.BasicHardKeyboardMap
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser
import io.github.lee0701.heonot.inputmethod.scripting.StringTreeExporter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject

class BasicHardKeyboard : HardKeyboard() {

	var layout = hashMapOf<Int, BasicHardKeyboardMap>()

	var shiftState = false
	var shiftPressing = false
	var altState = false
	var altPressing = false
	var capsLock = false
	var shiftInput = false

	var softLongPressMode = 0

	var automataState = 0
	var cho = 0
	var jung = 0
	var jong = 0

	@Subscribe
	override fun input(event: HardKeyEvent) {
		if(event.action == HardKeyEvent.HardKeyAction.RELEASE) {
			when(event.keyCode) {
				KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
					shiftState = false
				}
				KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> {
					altState = false
				}
			}
			return
		}

		when(event.keyCode) {
			KeyEvent.KEYCODE_DEL -> {
				EventBus.getDefault().post(BackspaceEvent())
				return
			}
			KeyEvent.KEYCODE_SPACE -> {
				EventBus.getDefault().post(SpecialKeyEvent(KeyEvent.KEYCODE_SPACE))
				return
			}
			KeyEvent.KEYCODE_ENTER -> {
				EventBus.getDefault().post(SpecialKeyEvent(KeyEvent.KEYCODE_ENTER))
				return
			}
			KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
				shiftState = true
			}
			KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> {
				altState = true
			}
		}

		val map = layout.get(event.keyCode)
		map?.let {
			val evaluator = HeonOt.getInstance().treeEvaluator
			evaluator.variables = hashMapOf(
				"T" to automataState.toLong(),
				"P" to if(capsLock) 1L else 0L,
				"D" to cho.toLong(), "E" to jung.toLong(), "F" to jong.toLong()
			)
			val result = evaluator.eval(if(shiftState) it.shift else it.normal)
			EventBus.getDefault().post(InputCharEvent(result))
			return@input
		}
		HardKeyboard.directInput(event.keyCode, shiftState, altState)
	}

	@Subscribe
	fun onSoftKey(event: SoftKeyEvent) {
		if(event.action == SoftKeyEvent.SoftKeyAction.PRESS) {
			when (event.keyCode) {
				KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
					if (!shiftState) {
						shiftState = true
					} else {
						if (!capsLock) {
							if (shiftInput) shiftState = false
							else capsLock = false
						} else {
							capsLock = false
							shiftState = false
						}
					}
					shiftInput = false
				}
				KeyEvent.KEYCODE_DEL -> {
					EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
					EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
				}
				else -> {
					if (softLongPressMode == LONG_PRESS_SHIFT) {
						if (event.type == SoftKeyEvent.SoftKeyPressType.LONG) {
							shiftState = true
							shiftInput = false
						}
					} else if (softLongPressMode == LONG_PRESS_REPEAT) {
						EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
					}
				}
			}
		} else if(event.action == SoftKeyEvent.SoftKeyAction.RELEASE) {
			when(event.keyCode) {
				KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
					shiftPressing = false
					if(shiftInput) {
						capsLock = false
						shiftState = false
					}
					shiftInput = false
				}
				KeyEvent.KEYCODE_DEL -> {

				}
				else -> {
					if (softLongPressMode == LONG_PRESS_SHIFT) {
						EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
						EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
					} else if (softLongPressMode == LONG_PRESS_REPEAT) {
						EventBus.getDefault().post(HardKeyEvent(null, HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
					}
					Handler().post {
						if(shiftState) shiftInput = true
						if (!capsLock && shiftInput && !shiftPressing) shiftState = false
					}
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

	override fun setProperty(key: String?, value: Any?) {
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
			else -> super.setProperty(key, value)
		}
	}

	override fun toJSONObject(): JSONObject {
		val obj = super.toJSONObject()
		val properties = JSONObject()

		properties.put("layout", storeLayout())

		obj.put("properties", properties)
		return obj
	}

	fun storeLayout() : JSONObject {
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

	companion object {

		val LONG_PRESS_SHIFT = 0
		val LONG_PRESS_REPEAT = 1

		fun loadLayout(layoutObject: JSONObject) : HashMap<Int, BasicHardKeyboardMap> {
			val parser = StringRecursionTreeParser()

			val result = hashMapOf<Int, BasicHardKeyboardMap>()
			val table = layoutObject.getJSONArray("layout")

			table?.let {
				for(i in 0 until table.length()) {
					val o = table.getJSONObject(i)

					val keyCode = o.getInt("keycode")
					val normal = parser.parse(o.getString("normal"))
					val shift = parser.parse(o.getString("shift"))

					result += keyCode to BasicHardKeyboardMap(keyCode, normal, shift)
				}
			}
			return result
		}
	}
}
