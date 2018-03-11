package io.github.lee0701.heonot.inputmethod.modules.global

import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.KeyStroke
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ShortcutProcessor : InputMethodModule() {

    var shortcuts: List<Shortcut> = arrayListOf()

    override fun init() {
        // do nothing
    }

    override fun pause() {
        // do nothing
    }

    @Subscribe(priority = 1)
    fun onHardKey(event: HardKeyEvent) {
        if (event.action === HardKeyEvent.HardKeyAction.PRESS && processShortcut(
                event.keyCode,
                event.isAltPressed,
                event.isShiftPressed
            )
        ) {
            EventBus.getDefault().cancelEventDelivery(event)
        }
    }

    fun processShortcut(keyCode: Int, altPressed: Boolean, shiftPressed: Boolean): Boolean {
        val evaluator = HeonOt.getInstance().treeEvaluator
        for (shortcut in shortcuts) {
            val keyStroke = shortcut.keyStroke
            if (keyStroke.keyCode == keyCode
                && keyStroke.isAlt == altPressed
                && keyStroke.isShift == shiftPressed
            ) {
                evaluator.variables = HeonOt.getInstance().variables
                val result = evaluator.eval(shortcut.treeNode)
                when (shortcut.mode) {
                    Shortcut.MODE_CHANGE -> HeonOt.getInstance().changeInputMethod(result.toInt())
                    Shortcut.MODE_INPUT -> EventBus.getDefault().post(CommitCharEvent(result.toChar(), 1))
                }
                return true
            }
        }
        return false
    }

    override fun clone(): ShortcutProcessor {
        // todo: implement me!
        return this
    }

    class Shortcut(var keyStroke: KeyStroke, var mode: Int, var treeNode: TreeNode?) {
        companion object {
            @JvmField
            val MODE_NONE = 0
            @JvmField
            val MODE_CHANGE = 1
            @JvmField
            val MODE_INPUT = 2
        }
    }
}
