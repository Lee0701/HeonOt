package io.github.lee0701.heonot.inputmethod.event

class ComposeCharEvent(val composingChar: String, val lastInput: Int, val cho: Int = 0, val jung: Int = 0, val jong: Int = 0) : Event()
