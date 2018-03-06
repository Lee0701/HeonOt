package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard;

public class KeyStroke {

	boolean control, alt, win, shift;
	int keyCode;

	public KeyStroke(boolean control, boolean alt, boolean win, boolean shift, int keyCode) {
		this.control = control;
		this.alt = alt;
		this.win = win;
		this.shift = shift;
		this.keyCode = keyCode;
	}

	public boolean isControl() {
		return control;
	}

	public boolean isAlt() {
		return alt;
	}

	public boolean isWin() {
		return win;
	}

	public boolean isShift() {
		return shift;
	}

	public int getKeyCode() {
		return keyCode;
	}
}
