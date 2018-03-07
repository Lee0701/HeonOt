package io.github.lee0701.heonot.inputmethod.event;

public final class InputCharEvent extends Event {
	private final Object character;

	public InputCharEvent(Object character) {
		this.character = character;
	}

	public Object getCharacter() {
		return character;
	}
}
