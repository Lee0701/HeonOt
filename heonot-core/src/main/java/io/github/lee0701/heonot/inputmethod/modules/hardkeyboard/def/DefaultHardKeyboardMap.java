package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.def;

public class DefaultHardKeyboardMap implements Cloneable {
	private final int keyCode;
	private final int normal;
	private final int shift;
	private final int caps;

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

	@Override
	public Object clone() {
		return new DefaultHardKeyboardMap(keyCode, normal, shift, caps);
	}
}
