package io.github.lee0701.heonot.inputmethod.event;

public class SpecialKeyEvent extends Event {

	final int keyCode;

	public SpecialKeyEvent(int keyCode) {
		this.keyCode = keyCode;
	}

	public int getKeyCode() {
		return keyCode;
	}

}
