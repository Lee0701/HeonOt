package me.blog.hgl1002.openwnn.KOKR.event;

public class SoftKeyEvent extends Event {

	private final SoftKeyAction action;

	private final int keyCode;

	private final SoftKeyPressType type;

	public SoftKeyEvent(SoftKeyAction action, int keyCode) {
		this(action, keyCode, SoftKeyPressType.SIGNLE);
	}

	public SoftKeyEvent(SoftKeyAction action, int keyCode, SoftKeyPressType type) {
		this.action = action;
		this.keyCode = keyCode;
		this.type = type;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public SoftKeyAction getAction() {
		return action;
	}

	public SoftKeyPressType getType() {
		return type;
	}

	public enum SoftKeyAction {
		PRESS, RELEASE, CANCEL;
	}

	public enum SoftKeyPressType {
		SIGNLE, LONG, FLICK_UP, FLICK_DOWN, FLICK_LEFT, FLICK_RIGHT;
	}

}
