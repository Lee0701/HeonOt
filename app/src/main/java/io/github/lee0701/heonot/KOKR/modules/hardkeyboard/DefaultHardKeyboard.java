package io.github.lee0701.heonot.KOKR.modules.hardkeyboard;

import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.method.MetaKeyKeyListener;
import android.util.Pair;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.lee0701.heonot.KOKR.event.UpdateStateEvent;
import io.github.lee0701.heonot.KOKR.modules.hardkeyboard.def.DefaultHardKeyboardMap;
import io.github.lee0701.heonot.KOKR.event.CommitCharEvent;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.InputCharEvent;
import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
import io.github.lee0701.heonot.KOKR.event.SetPropertyEvent;
import io.github.lee0701.heonot.KOKR.event.ShortcutEvent;
import io.github.lee0701.heonot.KOKR.event.SoftKeyEvent;
import io.github.lee0701.heonot.R;

public class DefaultHardKeyboard extends HardKeyboard {

	private Map<Integer, DefaultHardKeyboardMap> layout;

	boolean shiftInput;

	boolean shiftPressing;
	boolean altPressing;
	boolean capsLock;

	private static final int[] shiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
	private static final int[] altKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};

	public DefaultHardKeyboard() {

	}

	@Override
	public void init() {
		shiftPressing = altPressing = false;
		Event.fire(this, new SetPropertyEvent("soft-key-labels", getLabels(this.layout)));
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
		else if(e instanceof SetPropertyEvent) {
			SetPropertyEvent event = (SetPropertyEvent) e;
			this.setProperty(event.getKey(), event.getValue());
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		switch(key) {
		case "layout":
			try {
				if (value instanceof Map) {
					this.layout = (Map<Integer, DefaultHardKeyboardMap>) value;
				} else if (value instanceof JSONObject) {
					this.layout = loadLayout((JSONObject) value);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			break;
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

		if(layout == null) {
			directInput(event.getKeyCode());
			return;
		}
		DefaultHardKeyboardMap map = layout.get(event.getKeyCode());
		if(map != null) {
			int charCode = shiftPressing ? map.getShift() : map.getNormal();
			Event.fire(this, new InputCharEvent(charCode));
		} else {
			directInput(event.getKeyCode());
		}
	}

	private void directInput(int keyCode) {
		int hardShift = capsLock ? 2 : shiftPressing ? 1 : 0;
		int hardAlt = altPressing ? 1 : 0;
		int unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(keyCode, shiftKeyToggle[hardShift] | altKeyToggle[hardAlt]);
		Event.fire(this, new CommitCharEvent((char) unicodeChar, 1));
	}

	private void updateSoftKeyLabels() {
		Event.fire(this, new SetPropertyEvent("soft-key-labels", getLabels(this.layout)));
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
	public View createSettingsView(final Context context) {
		LinearLayout settings = new LinearLayout(context);
		KeyboardView keyboardView = new KeyboardView(context, null);
		Keyboard keyboard = new Keyboard(context, R.xml.keyboard_full_10cols);
		keyboardView.setKeyboard(keyboard);
		keyboardView.setPreviewEnabled(false);
		keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
			@Override
			public void onKey(final int primaryCode, int[] keyCodes) {
				createKeyEditDialog(context, primaryCode).show();
			}
			@Override
			public void onPress(int primaryCode) {}
			@Override
			public void onRelease(int primaryCode) {}
			@Override
			public void onText(CharSequence text) {}
			@Override
			public void swipeLeft() {}
			@Override
			public void swipeRight() {}
			@Override
			public void swipeDown() {}
			@Override
			public void swipeUp() {}
		});
		settings.addView(keyboardView);

		return settings;
	}

	public AlertDialog createKeyEditDialog(Context context, final int keyCode) {
		LinearLayout content = new LinearLayout(context);
		content.setOrientation(LinearLayout.VERTICAL);
		final EditText normal = new EditText(context);
		final EditText shift = new EditText(context);
		if(layout == null) {
			layout = new HashMap<>();
		}
		DefaultHardKeyboardMap map = layout.get(keyCode);
		if(map == null) layout.put(keyCode, map = new DefaultHardKeyboardMap(keyCode, 0, 0, 0));
		normal.setText(Integer.toString(map.getNormal()));
		shift.setText(Integer.toString(map.getShift()));
		content.addView(normal);
		content.addView(shift);
		return new AlertDialog.Builder(context)
				.setTitle("Key " + keyCode)
				.setView(content)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						layout.put(keyCode, new DefaultHardKeyboardMap(keyCode,
								Integer.parseInt(normal.getText().toString()),
								Integer.parseInt(shift.getText().toString()),
								Integer.parseInt(shift.getText().toString())));
					}
				})
				.setNeutralButton(R.string.button_delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						layout.remove(keyCode);
					}
				})
				.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.create();
	}

	@Override
	public JSONObject toJSONObject() throws JSONException {
		JSONObject object = super.toJSONObject();
		JSONArray properties = new JSONArray();

		if(layout != null) {
			JSONObject layout = new JSONObject();
			layout.put("key", "layout");
			layout.put("value", storeLayout());
			properties.put(layout);
		}

		object.put("properties", properties);

		return object;
	}

	public Map<Integer, DefaultHardKeyboardMap> getLayout() {
		return layout;
	}

	public void setLayout(Map<Integer, DefaultHardKeyboardMap> layout) {
		this.layout = layout;
	}

	public JSONObject storeLayout() throws JSONException {
		JSONObject object = new JSONObject();
		JSONArray layout = new JSONArray();

		for(Integer keyCode : this.layout.keySet()) {
			DefaultHardKeyboardMap map = this.layout.get(keyCode);
			JSONObject entry = new JSONObject();
			entry.put("keycode", (int) keyCode);
			entry.put("normal", Integer.toString(map.getNormal()));
			entry.put("shift", Integer.toString(map.getShift()));
			layout.put(entry);
		}

		object.put("layout", layout);
		return object;
	}

	public static Map<Integer, DefaultHardKeyboardMap> loadLayout(String layoutJson) throws JSONException {

		return loadLayout(new JSONObject(layoutJson));
	}

	public static Map<Integer, DefaultHardKeyboardMap> loadLayout(JSONObject layoutObject) throws JSONException {
		Map<Integer, DefaultHardKeyboardMap> layout = new HashMap<>();

		JSONArray table = layoutObject.getJSONArray("layout");
		if(table != null) {
			for(int i = 0 ; i < table.length() ; i++) {
				JSONObject o = table.getJSONObject(i);

				int keyCode = o.getInt("keycode");
				String normal = o.getString("normal");
				String shift = o.getString("shift");
				String caps = o.optString("caps", null);
				if(caps == null) caps = shift;

				DefaultHardKeyboardMap map = new DefaultHardKeyboardMap(keyCode,
						Integer.parseInt(normal), Integer.parseInt(shift), Integer.parseInt(caps));
				layout.put(keyCode, map);
			}
		}

		return layout;
	}

	@Override
	public Object clone() {
		DefaultHardKeyboard clone = new DefaultHardKeyboard();
		if(layout != null) {
			Map<Integer, DefaultHardKeyboardMap> layout = new HashMap<>();
			for(int i : this.layout.keySet()) {
				if(this.layout.get(i) != null) layout.put(i, (DefaultHardKeyboardMap) this.layout.get(i).clone());
			}
			clone.setLayout(layout);
		}
		clone.setName(getName());
		return clone;
	}
}
