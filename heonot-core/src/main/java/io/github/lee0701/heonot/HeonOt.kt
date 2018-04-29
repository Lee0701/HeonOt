package io.github.lee0701.heonot

import io.github.lee0701.heonot.inputmethod.InputMethod
import io.github.lee0701.heonot.inputmethod.event.CreateViewEvent
import io.github.lee0701.heonot.inputmethod.scripting.TreeEvaluator
import org.greenrobot.eventbus.EventBus

interface HeonOt {

	companion object {
		var INSTANCE: HeonOt? = null
	}

	val treeEvaluator: TreeEvaluator
	var currentInputMethod: InputMethod?
	var currentInputMethodId: Int
	val variables: Map<String, Long>
		get() = hashMapOf(
				"A" to currentInputMethodId.toLong()
				)
	var inputMethods: MutableList<InputMethod>
	var globalModules: InputMethod

	val modulePackageName: String

	fun init() {

	}

	fun destroy() {
		EventBus.getDefault().unregister(this)
		for (method in inputMethods) {
			method.pause()
		}
		globalModules.pause()

	}

	fun changeInputMethod(inputMethodId: Int) {
		if (inputMethodId == currentInputMethodId) return
		currentInputMethod?.pause()
		currentInputMethodId = inputMethodId
		currentInputMethod = inputMethods[currentInputMethodId]
		currentInputMethod?.registerListeners()
		currentInputMethod?.init()
		EventBus.getDefault().post(CreateViewEvent())
	}

	fun getInputMethodsCloned(): List<InputMethod> {
		val cloned = ArrayList<InputMethod>()
		for (method in inputMethods) {
			cloned.add(InputMethod(method))
		}
		return cloned
	}

	fun isSystemKey(keyCode: Int): Boolean

	fun directInput(keyCode: Int, shiftState: Boolean = false, altState: Boolean = false, capsLock: Boolean = false)

}