package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard

import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule

abstract class HardKeyboard : InputMethodModule() {
    abstract fun input(event: HardKeyEvent)
}
