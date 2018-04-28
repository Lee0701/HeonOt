package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard

import io.github.lee0701.heonot.*
import io.github.lee0701.heonot.inputmethod.event.*
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.def.DefaultHardKeyboardMap
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.HashMap

import io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.parseCharCode

open class DefaultHardKeyboard : HardKeyboard() {

	var layout: Map<Int, DefaultHardKeyboardMap> = hashMapOf()

	internal var shiftInput: Boolean = false

	internal var shiftState: Boolean = false
	internal var shiftPressing: Boolean = false
	internal var altState: Boolean = false
	internal var altPressing: Boolean = false
	internal var capsLock: Boolean = false

	var softLongPressMode: Int = 0

	override fun init() {
		altState = false
		shiftState = altState
		EventBus.getDefault().post(SetPropertyEvent("soft-key-labels", getLabels(this.layout)))
	}

	override fun pause() {

	}

	override fun setProperty(key: String, value: Any) {
		when (key) {
			"layout" -> try {
				if (value is Map<*, *>) {
					this.layout = value as Map<Int, DefaultHardKeyboardMap>
				} else if (value is JSONObject) {
					this.layout = loadLayout(value)
				}
			} catch (ex: Exception) {
				ex.printStackTrace()
			}

			"soft-long-press-mode" -> if (value is Int) {
				softLongPressMode = value
			}
		}
	}

	@Subscribe
	fun onSoftKey(event: SoftKeyEvent) {
		if (event.action === SoftKeyEvent.SoftKeyAction.PRESS) {
			when (event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftPressing = true
					if (event.type === SoftKeyEvent.SoftKeyPressType.SINGLE) {
						if (!shiftState) {
							shiftState = true
						} else {
							if (!capsLock) {
								if (shiftInput)
									shiftState = false
								else
									capsLock = true
							} else {
								capsLock = false
								shiftState = false
							}
						}
						shiftInput = false
						updateSoftKeyLabels()
					}
				}

				KEYCODE_DEL -> {
					EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
					EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
				}

				else -> when (softLongPressMode) {
					LONG_PRESS_SHIFT -> if (event.type === SoftKeyEvent.SoftKeyPressType.LONG) {
						shiftState = true
						shiftInput = false
						updateSoftKeyLabels()
					}

					LONG_PRESS_REPEAT -> EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
				}
			}
		} else if (event.action === SoftKeyEvent.SoftKeyAction.RELEASE) {
			when (event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftPressing = false
					if (shiftInput) {
						capsLock = false
						shiftState = false
					}
					shiftInput = false
					updateSoftKeyLabels()
				}

				KEYCODE_DEL -> {
				}

				else -> {
					when (softLongPressMode) {
						LONG_PRESS_SHIFT -> {
							EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.keyCode, 0, 0))
							EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
						}
						LONG_PRESS_REPEAT -> EventBus.getDefault().post(HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.keyCode, 0, 0))
					}
					//Handler().post({
						if (shiftState) shiftInput = true
						if (!capsLock && shiftInput && !shiftPressing) shiftState = false
						updateSoftKeyLabels()
					//})
				}
			}
		}
	}

	@Subscribe
	override fun input(event: HardKeyEvent) {
		if (event.action === HardKeyEvent.HardKeyAction.RELEASE) {
			when (event.keyCode) {
				KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
					shiftState = false
					updateSoftKeyLabels()
				}

				KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT -> altState = false
			}
			return
		}

		when (event.keyCode) {
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


			KEYCODE_ALT_LEFT, KEYCODE_ALT_RIGHT -> {
				altState = true
				return
			}

			KEYCODE_SHIFT_LEFT, KEYCODE_SHIFT_RIGHT -> {
				shiftState = true
				updateSoftKeyLabels()
				return
			}

			KEYCODE_CAPS_LOCK ->

				return
		}
		//TODO: Add text selection code.
		if (event.isCtrlPressed) {
			EventBus.getDefault().cancelEventDelivery(event)
			return
		}

		if (layout == null) {
			HeonOt.INSTANCE?.directInput(event.keyCode, shiftState, altState, capsLock)
			return
		}
		val map = layout[event.keyCode]
		if (map != null) {
			val charCode = if (shiftState) map.shift else map.normal
			EventBus.getDefault().post(InputCharEvent(charCode))
		} else {
			HeonOt.INSTANCE?.directInput(event.keyCode, shiftState, altState, capsLock)
		}
	}

	private fun updateSoftKeyLabels() {
		EventBus.getDefault().post(SetPropertyEvent("soft-key-labels", getLabels(this.layout)))
		EventBus.getDefault().post(UpdateStateEvent(UpdateStateEvent.Target.SOFT_KEYBOARD))
	}

	fun getLabels(table: Map<Int, DefaultHardKeyboardMap>): Map<Int, String> {
		val result = HashMap<Int, String>()
		for (keyCode in table.keys) {
			val map = table[keyCode]
			map?.let {
				val codePoint = if (shiftState) map.getShift() else map.getNormal()
				result[keyCode] = String(Character.toChars(codePoint))
			}
		}
		return result
	}

	@Throws(JSONException::class)
	override fun toJSONObject(): JSONObject {
		val `object` = super.toJSONObject()
		val properties = JSONObject()

		if (layout != null) {
			properties.put("layout", storeLayout())
		}

		properties.put("soft-long-press-mode", softLongPressMode)

		`object`.put("properties", properties)

		return `object`
	}

	@Throws(JSONException::class)
	fun storeLayout(): JSONObject {
		val `object` = JSONObject()
		val layout = JSONArray()

		for (map in this.layout) {
			val entry = JSONObject()
			entry.put("keycode", map.key)
			entry.put("normal", "0x" + Integer.toString(map.value.normal, 16))
			entry.put("shift", "0x" + Integer.toString(map.value.shift, 16))
			layout.put(entry)
		}

		`object`.put("layout", layout)
		return `object`
	}

	override fun clone(): DefaultHardKeyboard {
		val clone = DefaultHardKeyboard()
		val layout = HashMap<Int, DefaultHardKeyboardMap>()
		for (i in this.layout.keys) {
			this.layout[i]?.let {
				layout[i] = it.clone() as DefaultHardKeyboardMap
			}
		}
		clone.layout = layout
		clone.name = this.name
		return clone
	}

	companion object {

		val LONG_PRESS_SHIFT = 0
		val LONG_PRESS_REPEAT = 1

		@Throws(JSONException::class)
		fun loadLayout(layoutJson: String): Map<Int, DefaultHardKeyboardMap> {
			return loadLayout(JSONObject(layoutJson))
		}

		@Throws(JSONException::class)
		fun loadLayout(layoutObject: JSONObject): Map<Int, DefaultHardKeyboardMap> {
			val layout = HashMap<Int, DefaultHardKeyboardMap>()

			val table = layoutObject.getJSONArray("layout")
			if (table != null) {
				for (i in 0 until table.length()) {
					val o = table.getJSONObject(i)

					val keyCode = o.getInt("keycode")
					val normal = o.getString("normal")
					val shift = o.getString("shift")
					var caps: String? = o.optString("caps", null)
					if (caps == null) caps = shift

					val map = DefaultHardKeyboardMap(keyCode,
							parseCharCode(normal), parseCharCode(shift), parseCharCode(caps!!))
					layout[keyCode] = map
				}
			}

			return layout
		}
	}
}
