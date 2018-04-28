package io.github.lee0701.heonot.inputmethod.modules

import io.github.lee0701.heonot.HeonOt
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject

import io.github.lee0701.heonot.inputmethod.event.SetPropertyEvent

abstract class InputMethodModule : Cloneable {

	var name = "Module"

	abstract fun init()

	abstract fun pause()

	@Subscribe
	fun onSetProperty(event: SetPropertyEvent) {
		this.setProperty(event.key, event.value)
	}

	open fun setProperty(key: String, value: Any) {}

	public abstract override fun clone(): InputMethodModule

	@Throws(JSONException::class)
	open fun toJSONObject(): JSONObject {
		val obj = JSONObject()
		obj.put("name", this.name)
		val className = (HeonOt.INSTANCE?.let {javaClass.name.replace(it.modulePackageName + ".", "")} ?: javaClass.name).replace("io.github.lee0701.heonot.inputmethod.modules.", "")
		obj.put("class", className)
		return obj
	}

}
