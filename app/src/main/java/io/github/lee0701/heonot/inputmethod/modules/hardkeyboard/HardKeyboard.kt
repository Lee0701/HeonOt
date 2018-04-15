package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard

import android.text.method.MetaKeyKeyListener
import android.view.KeyCharacterMap
import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import org.greenrobot.eventbus.EventBus

abstract class HardKeyboard : InputMethodModule() {
    abstract fun input(event: HardKeyEvent)
    companion object {

        private val shiftKeyToggle = intArrayOf(0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED)
        private val altKeyToggle = intArrayOf(0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED)

        fun directInput(keyCode: Int, shiftState: Boolean = false, altState: Boolean = false, capsLock: Boolean = false) {
            val hardShift = if (capsLock) 2 else if (shiftState) 1 else 0
            val hardAlt = if (altState) 1 else 0
            val unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(keyCode, shiftKeyToggle[hardShift] or altKeyToggle[hardAlt])
            EventBus.getDefault().post(CommitCharEvent(unicodeChar.toChar(), 1))
        }

    }
}
