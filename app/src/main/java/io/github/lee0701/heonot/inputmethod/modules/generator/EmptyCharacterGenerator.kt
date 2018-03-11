package io.github.lee0701.heonot.inputmethod.modules.generator

import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent
import io.github.lee0701.heonot.inputmethod.event.InputCharEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class EmptyCharacterGenerator : CharacterGenerator() {
    override fun init() {
        // do nothing
    }

    override fun pause() {
        // do nothing
    }

    override fun input(code: Long) {
        EventBus.getDefault().post(CommitCharEvent(code.toChar(), 1))
    }

    override fun backspace() {
        // do nothing
    }

    @Subscribe
    fun onInputChar(event: InputCharEvent) {
        val o = event.character
        when (o) {
            is Long -> this.input(o)
            is Int -> this.input(o.toLong())
        }
    }

    override fun clone(): EmptyCharacterGenerator =
        EmptyCharacterGenerator().also {
            it.name = name
        }
}
