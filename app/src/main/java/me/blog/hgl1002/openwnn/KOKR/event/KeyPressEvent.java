package me.blog.hgl1002.openwnn.KOKR.event;

public class KeyPressEvent extends Event {
	private final int keyCode;
	private final boolean shift, caps;
	private final int repeated;

	public KeyPressEvent(int keyCode, boolean shift, boolean caps, int repeated) {
		this.keyCode = keyCode;
		this.shift = shift;
		this.caps = caps;
		this.repeated = repeated;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public boolean isShift() {
		return shift;
	}

	public boolean isCaps() {
		return caps;
	}

	public int getRepeated() {
		return repeated;
	}
}
