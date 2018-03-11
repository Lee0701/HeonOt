package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import io.github.lee0701.heonot.R;
import io.github.lee0701.heonot.inputmethod.event.*;
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.def.DefaultHardKeyboardMap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DefaultHardKeyboard extends HardKeyboard {

	private Map<Integer, DefaultHardKeyboardMap> layout;

	boolean shiftInput;

	boolean shiftState;
	boolean shiftPressing;
	boolean altState;
	boolean altPressing;
	boolean capsLock;

	int softLongPressMode;

	public static final int LONG_PRESS_SHIFT = 0;
	public static final int LONG_PRESS_REPEAT = 1;

	private static final int[] shiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
	private static final int[] altKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};

	public DefaultHardKeyboard() {

	}

	@Override
	public void init() {
		shiftState = altState = false;
		EventBus.getDefault().post(new SetPropertyEvent("soft-key-labels", getLabels(this.layout)));
	}

	@Override
	public void pause() {

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

		case "soft-long-press-mode":
			if(value instanceof Integer) {
				softLongPressMode = (Integer) value;
			}
			break;
		}
	}

	@Subscribe
	public void onSoftKey(SoftKeyEvent event) {
		if(event.getAction() == SoftKeyEvent.SoftKeyAction.PRESS) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftPressing = true;
				if(event.getType() == SoftKeyEvent.SoftKeyPressType.SINGLE) {
					if (!shiftState) {
						shiftState = true;
					} else {
						if (!capsLock) {
							if (shiftInput) shiftState = false;
							else capsLock = true;
						} else {
							capsLock = false;
							shiftState = false;
						}
					}
					shiftInput = false;
					updateSoftKeyLabels();
				}
				break;

			case KeyEvent.KEYCODE_DEL:
				EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.getKeyCode(), 0, 0));
				EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.getKeyCode(), 0, 0));
				break;

			default:
				switch(softLongPressMode) {
				case LONG_PRESS_SHIFT:
					if(event.getType() == SoftKeyEvent.SoftKeyPressType.LONG) {
						shiftState = true;
						shiftInput = false;
						updateSoftKeyLabels();
					}
					break;

				case LONG_PRESS_REPEAT:
					EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.getKeyCode(), 0, 0));
					break;

				}
				break;

			}
		} else if(event.getAction() == SoftKeyEvent.SoftKeyAction.RELEASE) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftPressing = false;
				if(shiftInput) {
					capsLock = false;
					shiftState = false;
				}
				shiftInput = false;
				updateSoftKeyLabels();
				break;

			case KeyEvent.KEYCODE_DEL:

				break;

			default:
				switch(softLongPressMode) {
				case LONG_PRESS_SHIFT:
					EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, event.getKeyCode(), 0, 0));
					EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.getKeyCode(), 0, 0));
					break;

				case LONG_PRESS_REPEAT:
					EventBus.getDefault().post(new HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, event.getKeyCode(), 0, 0));
					break;

				}
				new Handler().post(() -> {
					if(shiftState) shiftInput = true;
					if(!capsLock && shiftInput && !shiftPressing) shiftState = false;
					updateSoftKeyLabels();
				});
				break;

			}
		}
	}

	@Subscribe
	@Override
	public void input(HardKeyEvent event) {
		if(event.getAction() == HardKeyEvent.HardKeyAction.RELEASE) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_SHIFT_LEFT:
			case KeyEvent.KEYCODE_SHIFT_RIGHT:
				shiftState = false;
				updateSoftKeyLabels();
				break;

			case KeyEvent.KEYCODE_ALT_LEFT:
			case KeyEvent.KEYCODE_ALT_RIGHT:
				altState = false;
				break;

			}
			return;
		}

		switch(event.getKeyCode()) {
		case KeyEvent.KEYCODE_DEL:
			EventBus.getDefault().post(new BackspaceEvent());
			return;

		case KeyEvent.KEYCODE_SPACE:
			EventBus.getDefault().post(new CommitCharEvent(' ', 1));
			return;

		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
			altState = true;
			return;

		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
			shiftState = true;
			updateSoftKeyLabels();
			return;

		case KeyEvent.KEYCODE_CAPS_LOCK:

			return;

		}
		//TODO: Add text selection code.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (event.isCtrlPressed()) {
				EventBus.getDefault().cancelEventDelivery(event);
				return;
			}
		}

		if(layout == null) {
			directInput(event.getKeyCode());
			return;
		}
		DefaultHardKeyboardMap map = layout.get(event.getKeyCode());
		if(map != null) {
			int charCode = shiftState ? map.getShift() : map.getNormal();
			EventBus.getDefault().post(new InputCharEvent(charCode));
		} else {
			directInput(event.getKeyCode());
		}
	}

	private void directInput(int keyCode) {
		int hardShift = capsLock ? 2 : shiftState ? 1 : 0;
		int hardAlt = altState ? 1 : 0;
		int unicodeChar = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).get(keyCode, shiftKeyToggle[hardShift] | altKeyToggle[hardAlt]);
		EventBus.getDefault().post(new CommitCharEvent((char) unicodeChar, 1));
	}

	private void updateSoftKeyLabels() {
		EventBus.getDefault().post(new SetPropertyEvent("soft-key-labels", getLabels(this.layout)));
		EventBus.getDefault().post(new UpdateStateEvent());
	}

	public Map<Integer, String> getLabels(Map<Integer, DefaultHardKeyboardMap> table) {
		Map<Integer, String> result = new HashMap<>();
		if(table == null) return result;
		for(Integer keyCode : table.keySet()) {
			DefaultHardKeyboardMap map = table.get(keyCode);
			char charCode = (char) (shiftState ? map.getShift() : map.getNormal());
			result.put(keyCode, String.valueOf(charCode));
		}
		return result;
	}

	@Override
	public View createSettingsView(final Context context) {
		LinearLayout settings = new LinearLayout(context);
		settings.setOrientation(LinearLayout.VERTICAL);

		settings.addView(super.createSettingsView(context));

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
		CheckBox repeat = new CheckBox(context);
		repeat.setText(R.string.dsk_pref_soft_key_repeat);
		repeat.setOnCheckedChangeListener((v, checked) -> setSoftLongPressMode(checked ? LONG_PRESS_REPEAT : LONG_PRESS_SHIFT));
		settings.addView(repeat);

		return settings;
	}

	public AlertDialog createKeyEditDialog(Context context, final int keyCode) {
		LinearLayout content = new LinearLayout(context);
		content.setOrientation(LinearLayout.VERTICAL);
		final EditText normal = new EditText(context);
		normal.setHint(R.string.dhk_key_normal);
		normal.setEllipsize(TextUtils.TruncateAt.END);
		normal.setSingleLine(true);
		final EditText shift = new EditText(context);
		shift.setHint(R.string.dhk_key_shifted);
		shift.setEllipsize(TextUtils.TruncateAt.END);
		shift.setSingleLine(true);
		if(layout == null) {
			layout = new HashMap<>();
		}
		DefaultHardKeyboardMap map = layout.get(keyCode);
		if(map == null) layout.put(keyCode, map = new DefaultHardKeyboardMap(keyCode, 0, 0, 0));
		normal.setText("0x" + Integer.toHexString(map.getNormal()));
		shift.setText("0x" + Integer.toHexString(map.getShift()));
		TextInputLayout til;
		til = new TextInputLayout(context);
		til.addView(normal);
		content.addView(til);
		til = new TextInputLayout(context);
		til.addView(shift);
		content.addView(til);
		return new AlertDialog.Builder(context)
				.setTitle("Key " + keyCode)
				.setView(content)
				.setPositiveButton(R.string.button_ok, (dialog, which) -> {
					try {
						layout.put(keyCode, new DefaultHardKeyboardMap(keyCode,
								parseKeycode(normal.getText().toString()),
								parseKeycode(shift.getText().toString()),
								parseKeycode(shift.getText().toString())));
					} catch(NumberFormatException e) {
						Toast.makeText(context, R.string.msg_illegal_number_format, Toast.LENGTH_SHORT).show();
					}
				})
				.setNeutralButton(R.string.button_delete, (dialog, which) -> layout.remove(keyCode))
				.setNegativeButton(R.string.button_cancel, (dialog, which) -> {})
				.create();
	}

	private int parseKeycode(String str) {
		if(str.startsWith("0x")) {
			return Integer.parseInt(str.replaceFirst("0x", ""), 16);
		} else {
			try {
				return Integer.parseInt(str);
			} catch(NumberFormatException e) {
				if(str.length() == 1) {
					return str.charAt(0);
				}
				throw e;
			}
		}
	}

	@Override
	public JSONObject toJSONObject() throws JSONException {
		JSONObject object = super.toJSONObject();
		JSONObject properties = new JSONObject();

		if(layout != null) {
			properties.put("layout", storeLayout());
		}

		properties.put("soft-long-press-mode", softLongPressMode);

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
	public DefaultHardKeyboard clone() {
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

	public int getSoftLongPressMode() {
		return softLongPressMode;
	}

	public void setSoftLongPressMode(int softLongPressMode) {
		this.softLongPressMode = softLongPressMode;
	}
}
