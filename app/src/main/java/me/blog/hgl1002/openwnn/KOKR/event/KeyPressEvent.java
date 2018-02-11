package me.blog.hgl1002.openwnn.KOKR.event;

import android.view.KeyEvent;

public class KeyPressEvent extends Event {
	private final int keyCode;
	private final int repeated;
	private final int metaState;

	public KeyPressEvent(int keyCode, int metaState, int repeated) {
		this.keyCode = keyCode;
		this.metaState = metaState;
		this.repeated = repeated;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getMetaState() {
		return metaState;
	}

	public boolean isShiftPressed() {
		return (metaState & KeyEvent.META_SHIFT_ON) != 0;
	}

	public boolean isAltPressed() {
		return (metaState & KeyEvent.META_ALT_ON) != 0;
	}

	public int getRepeated() {
		return repeated;
	}

	public static class KeyReleaseEvent extends KeyPressEvent {
		public KeyReleaseEvent(int keyCode, int metaState, int repeated) {
			super(keyCode, metaState, repeated);
		}
	}

}
