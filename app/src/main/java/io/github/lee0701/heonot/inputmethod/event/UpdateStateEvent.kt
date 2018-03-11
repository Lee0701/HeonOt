package io.github.lee0701.heonot.inputmethod.event

class UpdateStateEvent @JvmOverloads constructor(
		val target: Target
) :
	Event() {

	enum class Target {
		SOFT_KEYBOARD, HARD_KEYBOARD, CHARACTER_GENERATOR
	}
}
