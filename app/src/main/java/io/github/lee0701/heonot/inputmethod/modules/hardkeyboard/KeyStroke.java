package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard;

import org.json.JSONException;
import org.json.JSONObject;

public class KeyStroke implements Cloneable {

	private boolean control, alt, win, shift;
	private int keyCode;

	public KeyStroke(boolean control, boolean alt, boolean win, boolean shift, int keyCode) {
		this.control = control;
		this.alt = alt;
		this.win = win;
		this.shift = shift;
		this.keyCode = keyCode;
	}

	public JSONObject toJsonObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("control", control);
			object.put("alt", alt);
			object.put("win", win);
			object.put("shift", shift);
			object.put("keycode", keyCode);
			return object;
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static KeyStroke fromJsonObject(JSONObject o) {
		try {
			return new KeyStroke(
					o.getBoolean("control"),
					o.getBoolean("alt"),
					o.getBoolean("win"),
					o.getBoolean("shift"),
					o.getInt("keycode"));
		} catch(JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public KeyStroke clone() {
		return new KeyStroke(control, alt, win, shift, keyCode);
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
