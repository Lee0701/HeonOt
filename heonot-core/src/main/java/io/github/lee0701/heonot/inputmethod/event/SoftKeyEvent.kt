package io.github.lee0701.heonot.inputmethod.event

class SoftKeyEvent @JvmOverloads constructor(
    val action: SoftKeyAction,
    val keyCode: Int,
    val type: SoftKeyPressType = SoftKeyPressType.SINGLE
) :
    Event() {

    enum class SoftKeyAction {
        PRESS, RELEASE, CANCEL
    }

    enum class SoftKeyPressType {
        SINGLE, LONG, FLICK_UP, FLICK_DOWN, FLICK_LEFT, FLICK_RIGHT, REPEAT
    }
}
