package io.github.lee0701.heonot.KOKR.event;

import android.os.Build;
import android.view.KeyEvent;

public class HardKeyEvent extends Event {

	private final HardKeyAction action;

	private final int keyCode;
	private final int repeated;
	private final int metaState;

	public HardKeyEvent(HardKeyAction action, int keyCode, int metaState, int repeated) {
		this.action = action;
		this.keyCode = keyCode;
		this.metaState = metaState;
		this.repeated = repeated;
	}

	public HardKeyAction getAction() {
		return action;
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

	public boolean isCtrlPressed() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return (metaState & KeyEvent.META_CTRL_ON) != 0;
		} else {
			return false;
		}
	}

	public int getRepeated() {
		return repeated;
	}

	public enum HardKeyAction {
		PRESS, RELEASE, CANCEL
	}

}
