package io.github.lee0701.heonot.inputmethod.event

class HardKeyEvent @JvmOverloads constructor(
    val action: HardKeyAction,
    val keyCode: Int,
    val metaState: Int,
    val repeated: Int
) : Event() {

    val isShiftPressed: Boolean
        get() = metaState and META_SHIFT_ON != 0

    val isAltPressed: Boolean
        get() = metaState and META_ALT_ON != 0

    val isCtrlPressed: Boolean
        get() = metaState and META_CTRL_ON != 0

    enum class HardKeyAction {
        PRESS, RELEASE, CANCEL
    }
    companion object {
        val META_ALT_ON = 0x02
    	val META_SHIFT_ON = 0x01
    	val META_CTRL_ON = 0x1000
    }
}
