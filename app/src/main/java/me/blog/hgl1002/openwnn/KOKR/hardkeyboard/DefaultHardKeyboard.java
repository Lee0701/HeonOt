package me.blog.hgl1002.openwnn.KOKR.hardkeyboard;

import android.os.Build;
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

import me.blog.hgl1002.openwnn.KOKR.event.CommitCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.DeleteCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Event;
import me.blog.hgl1002.openwnn.KOKR.event.InputCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.KeyPressEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;
import me.blog.hgl1002.openwnn.KOKR.event.SetPropertyEvent;
import me.blog.hgl1002.openwnn.KOKR.generator.UnicodeJamoHandler;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.def.DefaultHardKeyboardMap;

public class DefaultHardKeyboard implements HardKeyboard {

	List<Listener> listeners = new ArrayList<>();

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
			Event.fire(listeners, new SetPropertyEvent("combination-table", combinationTable));
		}

	}

	@Override
	public void init() {
		try {
			if(layoutJson != null) this.loadLayout(new JSONObject(layoutJson));
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
	}

	@Override
	public void input(KeyPressEvent event) {
		int key = event.getKeyCode();
		if(event instanceof KeyPressEvent.KeyReleaseEvent) {
			if(!shiftPressing) {
				if(key == KeyEvent.KEYCODE_SHIFT_LEFT || key == KeyEvent.KEYCODE_SHIFT_RIGHT){
					hardShift = 0;
					shiftPressing = true;
					if(capsLockShift) {
						hardShift = 2;
						shiftPressing = true;
						capsLockShift = false;
					}
				}
			}
			if(!altPressing) {
				if(key == KeyEvent.KEYCODE_ALT_LEFT || key == KeyEvent.KEYCODE_ALT_RIGHT){
					hardAlt = 0;
					altPressing = true;
				}
			}
			return;
		}

		switch(event.getKeyCode()) {
		case KeyEvent.KEYCODE_DEL:
			Event.fire(listeners, new DeleteCharEvent(1, 0));
			return;

		case KeyEvent.KEYCODE_SPACE:
			Event.fire(listeners, new CommitCharEvent(' ', 1));
			return;

		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			if (event.getRepeated() == 0) {
				if (++hardAlt > 2) { hardAlt = 0; }
			}
			altPressing = true;
			return;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			if (event.getRepeated() == 0) {
				if (++hardShift > 2) { hardShift = 0; }
			}
			shiftPressing = true;
			if(capsLock) {
				hardShift = 0;
				shiftPressing = false;
				capsLockShift = true;
			}
			return;

		case KeyEvent.KEYCODE_CAPS_LOCK:
			capsLock = !capsLock;
			if(capsLock) {
				hardShift = 2;
				shiftPressing = true;
			} else {
				hardShift = 0;
				shiftPressing = false;
			}
			return;

		}
		if((event.getMetaState() & KeyEvent.META_CAPS_LOCK_ON) != 0) {
			if(!capsLockShift) {
				capsLock = true;
				hardShift = 2;
				shiftPressing = true;
			}
		} else {
			if(capsLock) {
				capsLock = false;
				hardShift = 0;
				shiftPressing = false;
			}
		}
		if (hardShift == 1) {
			shiftPressing = false;
		}
		if (hardAlt == 1) {
			altPressing = false;
		}
		/*
		if (!ev.isAltPressed()) {
			if (hardAlt == 1) {
				hardAlt = 0;
			}
		}
		if (!ev.isShiftPressed()) {
			if (hardShift == 1) {
				hardShift = 0;
			}
		}
		*/
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
			Event.fire(listeners, new CommitCharEvent((char) unicodeChar, 1));
			return;
		}
		DefaultHardKeyboardMap map = table.get(event.getKeyCode());
		if(map != null) {
			int charCode = hardShift > 0 ? map.getShift() : map.getNormal();
			Event.fire(listeners, new InputCharEvent(charCode));
		}
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
