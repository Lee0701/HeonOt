package me.blog.hgl1002.openwnn.KOKR.hardkeyboard.def;

public class DefaultHardKeyboardMap {
	private final int keyCode, normal, shift;

	public DefaultHardKeyboardMap(int keyCode, int normal, int shift) {
		this.keyCode = keyCode;
		this.normal = normal;
		this.shift = shift;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getNormal() {
		return normal;
	}

	public int getShift() {
		return shift;
	}
}
