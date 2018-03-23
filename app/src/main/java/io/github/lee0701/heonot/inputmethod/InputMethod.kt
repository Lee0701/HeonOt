package io.github.lee0701.heonot.inputmethod

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.modules.softkeyboard.SoftKeyboard
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class InputMethod {
    var name: String = ""
    var modules: List<InputMethodModule>


    constructor(modules: List<InputMethodModule>) {
        this.modules = modules
    }

    constructor(original: InputMethod) {
        this.name = original.name
        this.modules = original.modules.map { it.clone() }
    }

    fun registerListeners() {
        modules.forEach {
            EventBus.getDefault().register(it)
        }
    }

    fun clearListeners() {
        modules.forEach {
            EventBus.getDefault().unregister(it)
        }
    }

    fun init() {
        modules.forEach {
            it.init()
        }
    }

    fun pause() {
        modules.forEach {
            it.pause()
            EventBus.getDefault().unregister(it)
        }
    }

    fun createView(context: Context): View {
        val view = LinearLayout(context)
        for (module in modules) {
            if (module is SoftKeyboard) {
                view.addView(module.createView(context))
            }
        }
        return view
    }

    @Throws(JSONException::class)
    fun toJSON(indentSpaces: Int): String {
        val methodObject = JSONObject()

        methodObject.put("name", name)

        val modules = JSONArray()

        this.modules.forEach {
            modules.put(it.toJSONObject())
        }

        methodObject.put("modules", modules)
        return if (indentSpaces > 0) {
            methodObject.toString(indentSpaces)
        } else {
            methodObject.toString()
        }
    }

    @Deprecated("Use copy constructor instead", replaceWith = ReplaceWith("InputMethod"))
            /**
             * It is deprecated. Use copy constructor instead.
             */
    fun clone(): Any {
        return InputMethod(this)
    }

    companion object {
        @JvmStatic
        @Throws(JSONException::class)
        fun loadJSON(methodJson: String): InputMethod {
            val methodObject = JSONObject(methodJson)

            val modulesArray = methodObject.optJSONArray("modules")

            val modules = arrayListOf<InputMethodModule>()
            if (modulesArray != null) {
                for (i in 0 until modulesArray.length()) {
                    val module = modulesArray.getJSONObject(i)
                    val name = module.optString("name")
                    val className = module.getString("class")
                    try {
                        val moduleClass = Class.forName(className)
                        val m = moduleClass.getDeclaredConstructor().newInstance() as InputMethodModule
                        m.name = name
                        val props = module.optJSONObject("properties")
                        if (props?.names() != null) {
                            val names = props.names()
                            for (j in 0 until names.length()) {
                                val key = names.getString(j)
                                m.setProperty(key, props.opt(key))
                            }
                        }
                        modules.add(m)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }

            val arr = arrayOfNulls<InputMethodModule>(modules.size)
            val method = InputMethod(modules)
            method.name = methodObject.optString("name")

            return method
        }
    }
}
