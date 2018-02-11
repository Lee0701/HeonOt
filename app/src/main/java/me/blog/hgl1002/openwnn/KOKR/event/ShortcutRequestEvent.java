package me.blog.hgl1002.openwnn.KOKR.event;

public class ShortcutRequestEvent extends Event {
	private final int keyCode;
	private final boolean altPressed, shiftPressed;

	public ShortcutRequestEvent(int keyCode, boolean altPressed, boolean shiftPressed) {
		this.keyCode = keyCode;
		this.altPressed = altPressed;
		this.shiftPressed = shiftPressed;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public boolean isAltPressed() {
		return altPressed;
	}

	public boolean isShiftPressed() {
		return shiftPressed;
	}
}
