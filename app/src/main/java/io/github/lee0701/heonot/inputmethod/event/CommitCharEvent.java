package io.github.lee0701.heonot.inputmethod.event;

public final class CommitCharEvent extends Event {
	private final char character;
	private final int cursorPosition;

	public CommitCharEvent(char character, int cursorPosition) {
		this.character = character;
		this.cursorPosition = cursorPosition;
	}

	public char getCharacter() {
		return character;
	}

	public int getCursorPosition() {
		return cursorPosition;
	}
}
