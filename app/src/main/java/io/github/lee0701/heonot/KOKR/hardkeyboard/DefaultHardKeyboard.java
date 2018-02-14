package io.github.lee0701.heonot.KOKR.hardkeyboard;

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

import io.github.lee0701.heonot.KOKR.event.UpdateStateEvent;
import io.github.lee0701.heonot.KOKR.hardkeyboard.def.DefaultHardKeyboardMap;
import io.github.lee0701.heonot.KOKR.event.CommitCharEvent;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.InputCharEvent;
import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
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

	boolean shiftInput;

	boolean shiftPressing;
	boolean altPressing;
	boolean capsLock;

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
		shiftPressing = altPressing = false;
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof HardKeyEvent) {
			HardKeyEvent event = (HardKeyEvent) e;
			this.input(event);
		}
		else if(e instanceof SoftKeyEvent) {
			SoftKeyEvent event = (SoftKeyEvent) e;
			onSoftKey(event);
		}
	}

	private void onSoftKey(SoftKeyEvent event) {
		if(event.getAction() == SoftKeyEvent.SoftKeyAction.PRESS) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				if(!shiftPressing) {
					shiftPressing = true;
				} else {
					if(!capsLock) {
						if(shiftInput) shiftPressing = false;
						else capsLock = true;
					} else {
						capsLock = false;
						shiftPressing = false;
					}
				}
				shiftInput = false;
				updateSoftKeyLabels();
				break;

			default:
				if(event.getType() == SoftKeyEvent.SoftKeyPressType.LONG) {
					shiftPressing = true;
					shiftInput = false;
					updateSoftKeyLabels();
				}
				break;

			}
		} else if(event.getAction() == SoftKeyEvent.SoftKeyAction.RELEASE) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				if(shiftInput) {
					capsLock = false;
					shiftPressing = false;
				}
				shiftInput = false;
				updateSoftKeyLabels();
				break;

			default:
				input(new HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.getKeyCode(), 0, 0));
				input(new HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.getKeyCode(), 0, 0));
				if(shiftPressing) shiftInput = true;
				if(!capsLock && shiftInput) shiftPressing = false;
				updateSoftKeyLabels();
				break;

			}
		}
	}

	@Override
	public void input(HardKeyEvent event) {
		if(event.getAction() == HardKeyEvent.HardKeyAction.RELEASE) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftPressing = false;
				updateSoftKeyLabels();
				break;

			case KeyEvent.KEYCODE_ALT_LEFT:
			case KeyEvent.KEYCODE_ALT_RIGHT:
				altPressing = false;
				break;

			}
			return;
		}

		ShortcutEvent req = new ShortcutEvent(event.getKeyCode(),
				altPressing, shiftPressing);
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
			altPressing = true;
			return;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftPressing = true;
			updateSoftKeyLabels();
			return;

		case KeyEvent.KEYCODE_CAPS_LOCK:

			return;

		}
		//TODO: Add text selection code.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (event.isCtrlPressed()) {
//				event.setCancelled(true);
				return;
			}
		}

		if(table == null) {
			int hardShift = capsLock ? 2 : shiftPressing ? 1 : 0;
			int hardAlt = altPressing ? 1 : 0;
			int unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(event.getKeyCode(), shiftKeyToggle[hardShift] | altKeyToggle[hardAlt]);
			Event.fire(this, new CommitCharEvent((char) unicodeChar, 1));
			return;
		}
		DefaultHardKeyboardMap map = table.get(event.getKeyCode());
		if(map != null) {
			int charCode = shiftPressing ? map.getShift() : map.getNormal();
			Event.fire(this, new InputCharEvent(charCode));
		}
	}

	private void updateSoftKeyLabels() {
		Event.fire(this, new SetPropertyEvent("soft-key-labels", getLabels(this.table)));
		Event.fire(this, new UpdateStateEvent());
	}

	public Map<Integer, String> getLabels(Map<Integer, DefaultHardKeyboardMap> table) {
		Map<Integer, String> result = new HashMap<>();
		if(table == null) return result;
		for(Integer keyCode : table.keySet()) {
			DefaultHardKeyboardMap map = table.get(keyCode);
			char charCode = (char) (shiftPressing ? map.getShift() : map.getNormal());
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
