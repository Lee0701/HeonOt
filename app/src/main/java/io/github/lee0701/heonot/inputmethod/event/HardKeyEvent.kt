package io.github.lee0701.heonot.inputmethod.event

import android.os.Build
import android.view.KeyEvent


class HardKeyEvent @JvmOverloads constructor(
    private val originalEvent: KeyEvent? = null,
    val action: HardKeyAction,
    val keyCode: Int,
    val metaState: Int,
    val repeated: Int
) : Event() {

    val isShiftPressed: Boolean
        get() = metaState and KeyEvent.META_SHIFT_ON != 0

    val isAltPressed: Boolean
        get() = metaState and KeyEvent.META_ALT_ON != 0

    val isCtrlPressed: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            metaState and KeyEvent.META_CTRL_ON != 0
        } else {
            false
        }

    enum class HardKeyAction {
        PRESS, RELEASE, CANCEL
    }
}
