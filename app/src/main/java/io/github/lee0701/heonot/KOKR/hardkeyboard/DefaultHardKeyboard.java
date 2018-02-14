package io.github.lee0701.heonot.KOKR.hardkeyboard;

import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lee0701.heonot.KOKR.hardkeyboard.def.DefaultHardKeyboardMap;
import io.github.lee0701.heonot.KOKR.event.CommitCharEvent;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.InputCharEvent;
import io.github.lee0701.heonot.KOKR.event.KeyPressEvent;
import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.SetPropertyEvent;
import io.github.lee0701.heonot.KOKR.event.ShortcutEvent;
import io.github.lee0701.heonot.KOKR.event.SoftKeyEvent;
import io.github.lee0701.heonot.KOKR.generator.UnicodeJamoHandler;

public class DefaultHardKeyboard implements HardKeyboard {

	List<EventListener> listeners = new ArrayList<>();

	String layoutJson;

	private Map<Integer, DefaultHardKeyboardMap> table;

	private Map<UnicodeJamoHandler.JamoPair, Character> combinationTable;

	int hardShift;
	int hardAlt;
	boolean shiftPressing;
	boolean altPressing;
	boolean capsLock;
	boolean capsLockShift;

	private static final int[] shiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
	private static final int[] altKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};

	boolean selectionMode;
	int selectionStart, selectionEnd;

	public DefaultHardKeyboard() {

	}

	public DefaultHardKeyboard(String layoutJson) {
		this.layoutJson = layoutJson;
	}

	private void loadLayout(JSONObject layout) throws JSONException {

		this.table = new HashMap<>();
		this.combinationTable = new HashMap<>();

		JSONArray table = layout.getJSONArray("table");
		JSONArray combination = layout.getJSONArray("combination");

		if(table != null) {
			for(int i = 0 ; i < table.length() ; i++) {
				JSONObject o = table.getJSONObject(i);

				int keyCode = o.getInt("keycode");
				String normal = o.getString("normal");
				String shift = o.getString("shift");

				DefaultHardKeyboardMap map = new DefaultHardKeyboardMap(keyCode, Integer.parseInt(normal), Integer.parseInt(shift));
				this.table.put(keyCode, map);
			}
		}

		if(combination != null) {
			for(int i = 0 ; i < combination.length() ; i++) {
				JSONObject o = combination.getJSONObject(i);
				int a = o.getInt("a");
				int b = o.getInt("b");
				String result = o.getString("result");
				UnicodeJamoHandler.JamoPair pair = new UnicodeJamoHandler.JamoPair((char) a, (char) b);
				combinationTable.put(pair, (char) Integer.parseInt(result));
			}
		}

	}

	@Override
	public void init() {
		try {
			if(layoutJson != null) {
				this.loadLayout(new JSONObject(layoutJson));
				Event.fire(this, new SetPropertyEvent("soft-key-labels", getLabels(this.table)));
				Event.fire(this, new SetPropertyEvent("combination-table", combinationTable));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		hardShift = hardAlt = 0;
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof KeyPressEvent) {
			KeyPressEvent event = (KeyPressEvent) e;
			this.input(event);
		}
		else if(e instanceof SoftKeyEvent) {
			SoftKeyEvent event = (SoftKeyEvent) e;
			if(event.getAction() == SoftKeyEvent.SoftKeyAction.PRESS) {
				input(new KeyPressEvent(event.getKeyCode(), 0, 0));
			} else if(event.getAction() == SoftKeyEvent.SoftKeyAction.RELEASE) {
				input(new KeyPressEvent.KeyReleaseEvent(event.getKeyCode(), 0, 0));
			}
		}
	}

	@Override
	public void input(KeyPressEvent event) {
		if(event instanceof KeyPressEvent.KeyReleaseEvent) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				hardShift = 0;
				shiftPressing = false;
				break;

			case KeyEvent.KEYCODE_ALT_LEFT:
			case KeyEvent.KEYCODE_ALT_RIGHT:
				hardAlt = 0;
				altPressing = false;
				break;

			}
			return;
		}

		ShortcutEvent req = new ShortcutEvent(event.getKeyCode(),
				hardAlt > 0, hardShift > 0);
		Event.fire(this, req);
		if(req.isCancelled()) return;

		switch(event.getKeyCode()) {
		case KeyEvent.KEYCODE_DEL:
			Event.fire(this, new DeleteCharEvent(1, 0));
			return;

		case KeyEvent.KEYCODE_SPACE:
			Event.fire(this, new CommitCharEvent(' ', 1));
			return;

		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			hardAlt = 1;
			altPressing = true;
			return;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			hardShift = 1;
			shiftPressing = false;
			return;

		case KeyEvent.KEYCODE_CAPS_LOCK:

			return;

		}
		/*
		if (shiftPressing) {
			switch () {
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!selectionMode) {
					selectionEnd = mInputConnection.getTextBeforeCursor(Integer.MAX_VALUE, 0).length();
					selectionStart = selectionEnd;
					selectionMode = true;
				}
				if (selectionMode) {
					if (key == KeyEvent.KEYCODE_DPAD_LEFT) selectionEnd--;
					if (key == KeyEvent.KEYCODE_DPAD_RIGHT) selectionEnd++;
					if (key == KeyEvent.KEYCODE_DPAD_UP) {
						int i = 1;
						CharSequence text = "";
						boolean end;
						while(!(end = mInputConnection.getTextBeforeCursor(i, 0).equals(text)) && (text = mInputConnection.getTextBeforeCursor(i, 0)).charAt(0) != '\n') i++;
						if(end) selectionEnd -= mInputConnection.getTextBeforeCursor(Integer.MAX_VALUE, 0).length();
						else selectionEnd -= i;
					}
					if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
						int i = 1;
						CharSequence text = "";
						boolean end;
						while(!(end = mInputConnection.getTextAfterCursor(i, 0).equals(text)) && (text = mInputConnection.getTextAfterCursor(i, 0)).charAt(text.length()-1) != '\n') i++;
						if(end) selectionEnd += mInputConnection.getTextAfterCursor(Character.MAX_VALUE, 0).length();
						else selectionEnd += i;
					}
					int start = selectionStart, end = selectionEnd;
					if (selectionStart > selectionEnd) {
						start = selectionEnd;
						end = selectionStart;
					}
					mInputConnection.setSelection(start, end);
				}
				return true;

			default:
				selectionMode = false;
				break;
			}
		} else {
			selectionMode = false;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (ev.isCtrlPressed()) return false;
		}
		*/

		if(table == null) {
			int unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(event.getKeyCode(), shiftKeyToggle[hardShift] | altKeyToggle[hardAlt]);
			Event.fire(this, new CommitCharEvent((char) unicodeChar, 1));
			return;
		}
		DefaultHardKeyboardMap map = table.get(event.getKeyCode());
		if(map != null) {
			int charCode = hardShift > 0 ? map.getShift() : map.getNormal();
			Event.fire(this, new InputCharEvent(charCode));
		}
	}

	public Map<Integer, String> getLabels(Map<Integer, DefaultHardKeyboardMap> table) {
		Map<Integer, String> result = new HashMap<>();
		if(table == null) return result;
		for(Integer keyCode : table.keySet()) {
			DefaultHardKeyboardMap map = table.get(keyCode);
			char charCode = (char) (hardShift > 0 ? map.getShift() : map.getNormal());
			result.put(keyCode, String.valueOf(charCode));
		}
		return result;
	}

	@Override
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public List<EventListener> getListeners() {
		return listeners;
	}
}
