package io.github.lee0701.heonot.KOKR.modules.hardkeyboard.def;

public class DefaultHardKeyboardMap {
	private final int keyCode, normal, shift, caps;

	public DefaultHardKeyboardMap(int keyCode, int normal, int shift, int caps) {
		this.keyCode = keyCode;
		this.normal = normal;
		this.shift = shift;
		this.caps = caps;
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

	public int getCaps() {
		return caps;
	}
}
