package io.github.lee0701.heonot.inputmethod.modules.generator

import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule

abstract class CharacterGenerator : InputMethodModule() {
    abstract fun input(code: Long)

    abstract fun backspace()
}
