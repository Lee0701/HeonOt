package io.github.lee0701.heonot.android

import android.content.Context
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.text.TextUtils
import android.text.method.MetaKeyKeyListener
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.InputMethodLoader
import io.github.lee0701.heonot.android.inputmethod.modules.AndroidViewCreatable
import io.github.lee0701.heonot.inputmethod.InputMethod
import io.github.lee0701.heonot.inputmethod.event.*
import io.github.lee0701.heonot.inputmethod.scripting.TreeEvaluator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.NoSubscriberEvent
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import java.io.File
import java.io.IOException

class AndroidHeonOt : InputMethodService, HeonOt {

	constructor() : super() {
		HeonOt.INSTANCE?.destroy()
		HeonOt.INSTANCE = this
	}

	constructor(context: Context) : this() {
		attachBaseContext(context)
	}

	override var treeEvaluator = TreeEvaluator()
	override var currentInputMethodId = 0
	override var currentInputMethod: InputMethod? = null
	override var inputMethods = mutableListOf<InputMethod>()
	override var globalModules: InputMethod = InputMethod(mutableListOf())

	override val modulePackageName: String = "io.github.lee0701.heonot.android.inputmethod.modules"

	private val shiftKeyToggle = intArrayOf(0, HardKeyEvent.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED)
	private val altKeyToggle = intArrayOf(0, HardKeyEvent.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED)

	override fun onCreate() {
		super.onCreate()
		init()
	}

	override fun onDestroy() {
		super.onDestroy()
		this.destroy()
	}

	override fun init() {
		val methodsDir = File(filesDir, "methods")
		val globalMethodsFile = File(filesDir, "global.json")

		if (!methodsDir.exists()) {
			methodsDir.mkdir()
			try {
				val method = getRawString("method_qwerty")
				inputMethods.add(InputMethod.loadJSON(method))
			} catch (e: JSONException) {
				e.printStackTrace()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			try {
				val method = getRawString("method_shin_original")
				inputMethods.add(InputMethod.loadJSON(method))
			} catch (e: JSONException) {
				e.printStackTrace()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			InputMethodLoader.storeMethods(methodsDir, inputMethods)
		} else {
			inputMethods = InputMethodLoader.loadMethods(methodsDir)
		}

		if (!globalMethodsFile.exists()) {
			try {
				val method = getRawString("global_modules")
				globalModules = InputMethod.loadJSON(method)
			} catch (e: JSONException) {
				e.printStackTrace()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			InputMethodLoader.storeMethod(globalMethodsFile, globalModules)
		} else {
			globalModules = InputMethodLoader.loadMethod(globalMethodsFile) ?: globalModules
		}
		currentInputMethod = inputMethods[0].apply {
			registerListeners()
			init()
		}
		globalModules.registerListeners()
		EventBus.getDefault().register(this)
	}

	override fun onCreateInputView(): View {
		val view = LinearLayout(this)
		view.orientation = LinearLayout.VERTICAL
		currentInputMethod?.let {
			for (module in it.modules) {
				if (module is AndroidViewCreatable) {
					view.addView(module.createView(this))
				}
			}
		}
		return view
	}

	override fun onStartInputView(attribute: EditorInfo, restarting: Boolean) {
		commitComposingChar()
		if (restarting) {
			super.onStartInputView(attribute, restarting)
		} else {
			super.onStartInputView(attribute, restarting)
			EventBus.getDefault().post(InputTypeEvent(attribute.inputType))
		}

	}

	override fun onStartInput(attribute: EditorInfo, restarting: Boolean) {
		super.onStartInput(attribute, restarting)
	}

	override fun onCreateCandidatesView(): View {
		return LinearLayout(this)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		try {
			super.onConfigurationChanged(newConfig)

			if (currentInputConnection != null) {
				val hiddenState = newConfig.hardKeyboardHidden
				val hidden = hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES
				EventBus.getDefault().post(HardwareChangeEvent(!hidden))
			}
		} catch (ex: Exception) {
		}

	}

	override fun onKeyDown(keyCode: Int, e: KeyEvent): Boolean {
		if (isSystemKey(e.keyCode)) return super.onKeyDown(keyCode, e)
		val event = HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, keyCode, e.metaState, e.repeatCount)
		EventBus.getDefault().post(event)
		return true
	}

	override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean {
		if (isSystemKey(e.keyCode)) return super.onKeyUp(keyCode, e)
		val event = HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, keyCode, e.metaState, e.repeatCount)
		EventBus.getDefault().post(event)
		return true
	}

	override fun isSystemKey(keyCode: Int): Boolean {
		when (keyCode) {
			KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_MUTE, KeyEvent.KEYCODE_VOLUME_UP -> return true
		}
		return false
	}

	override fun onFinishInput() {
		super.onFinishInput()
	}

	override fun onViewClicked(focusChanged: Boolean) {
		super.onViewClicked(focusChanged)
		commitComposingChar()
	}

	private fun commitComposingChar() {
		EventBus.getDefault().post(CommitComposingCharEvent())
	}

	override fun hideWindow() {

		super.hideWindow()
	}

	override fun onEvaluateFullscreenMode(): Boolean {
		return false
	}

	override fun onEvaluateInputViewShown(): Boolean {
		super.onEvaluateInputViewShown()
		return true
	}

	@Throws(IOException::class)
	private fun getRawString(resName: String): String {
		val stream = resources.openRawResource(resources.getIdentifier(resName, "raw", packageName))
		val bytes = ByteArray(stream.available())
		stream.read(bytes)
		return String(bytes)
	}

	@Subscribe
	fun onNoSubscriber(event: NoSubscriberEvent) {

	}

	@Subscribe
	fun onCreateView(event: CreateViewEvent) {
		setInputView(onCreateInputView())
	}

	@Subscribe
	fun onSpecialKey(event: SpecialKeyEvent) {
		val editorInfo = currentInputEditorInfo
		when (event.keyCode) {
			KeyEvent.KEYCODE_ENTER -> when (editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) {
				EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_GO -> {
					currentInputConnection.finishComposingText()
					EventBus.getDefault().post(CommitComposingCharEvent())
					sendDefaultEditorAction(true)
				}

				else -> EventBus.getDefault().post(KeyCharEvent('\n'))
			}

			KeyEvent.KEYCODE_SPACE -> EventBus.getDefault().post(KeyCharEvent(' '))
		}
	}

	@Subscribe
	fun onComposeChar(event: ComposeCharEvent) {
		val composing = event.composingChar
		currentInputConnection.setComposingText(composing, 1)
	}

	@Subscribe
	fun onFinishComposing(event: FinishComposingEvent) {
		currentInputConnection.finishComposingText()
	}

	@Subscribe
	fun onKeyChar(event: KeyCharEvent) {
		val ic = currentInputConnection
		ic.finishComposingText()
		EventBus.getDefault().post(CommitComposingCharEvent())
		sendKeyChar(event.character)
	}

	@Subscribe
	fun onCommitChar(event: CommitCharEvent) {
		val ic = currentInputConnection
		ic.finishComposingText()
		EventBus.getDefault().post(CommitComposingCharEvent())
		ic.commitText(event.character.toString(), event.cursorPosition)
	}

	@Subscribe
	fun onCommitString(event: CommitStringEvent) {
		val ic = currentInputConnection
		ic.finishComposingText()
		EventBus.getDefault().post(CommitComposingCharEvent())
		ic.commitText(event.string, event.cursorPosition)
	}

	@Subscribe
	fun onBackspace(event: BackspaceEvent) {
		EventBus.getDefault().post(DeleteCharEvent(1, 0))
	}

	@Subscribe
	fun onDeleteChar(event: DeleteCharEvent) {
		val ic = currentInputConnection
		val selected = ic.getSelectedText(0)
		if (TextUtils.isEmpty(selected)) {
			ic.deleteSurroundingText(event.beforeLength, event.afterLength)
		} else {
			ic.commitText("", 1)
		}
	}

	override fun directInput(keyCode: Int, shiftState: Boolean, altState: Boolean, capsLock: Boolean) {
		val hardShift = if (capsLock) 2 else if (shiftState) 1 else 0
		val hardAlt = if (altState) 1 else 0
		val unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(keyCode, shiftKeyToggle[hardShift] or altKeyToggle[hardAlt])
		EventBus.getDefault().post(CommitCharEvent(unicodeChar.toChar(), 1))
	}

}