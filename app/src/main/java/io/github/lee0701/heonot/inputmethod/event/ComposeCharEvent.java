package io.github.lee0701.heonot.inputmethod.event;

public final class ComposeCharEvent extends Event {
	private final String composingChar;
	private final int lastInput;

	public ComposeCharEvent(String composingChar, int lastInput) {
		this.composingChar = composingChar;
		this.lastInput = lastInput;
	}

	public String getComposingChar() {
		return composingChar;
	}

	public int getLastInput() {
		return lastInput;
	}
}
