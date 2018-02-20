package io.github.lee0701.heonot.KOKR.modules.softkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.KeyboardKOKR;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.SetPropertyEvent;
import io.github.lee0701.heonot.KOKR.event.SoftKeyEvent;
import io.github.lee0701.heonot.KOKR.event.SoftKeyEvent.*;
import io.github.lee0701.heonot.KOKR.event.UpdateStateEvent;
import io.github.lee0701.heonot.R;

public class DefaultSoftKeyboard extends SoftKeyboard implements KeyboardView.OnKeyboardActionListener {

	protected static final int DEFAULT_FLICK_SENSITIVITY = 100;

	protected static final int SPACE_SLIDE_UNIT = 30;
	protected static final int BACKSPACE_SLIDE_UNIT = 250;

	public static final boolean PORTRAIT = false;
	public static final boolean LANDSCAPE = true;

	protected ViewGroup mainView, subView;
	protected KeyboardView keyboardView;

	protected String keyboardResName;
	protected int keyboardResId;
	protected KeyboardKOKR keyboard;

	protected Vibrator vibrator;
	protected MediaPlayer sound;

	protected boolean disableKeyInput;

	protected boolean displayMode;

	private int keyHeightPortrait = 50, keyHeightLandscape = 42;
	private int longPressTimeout = 300;
	private boolean useFlick;
	private int flickSensitivity = 100, spaceSlideSensitivity = 100;
	private int vibrateDuration = 30;
	private boolean showPreview = false;
	private int keyIcon = 0;


	protected Map<Integer, String> labels;

/*
	SparseArray<SparseArray<Integer>> mKeyIcons = new SparseArray<SparseArray<Integer>>() {{
		put(0, new SparseArray<Integer>() {{
			put(KEYCODE_QWERTY_SHIFT, R.drawable.key_qwerty_shift);
			put(KEYCODE_QWERTY_ENTER, R.drawable.key_qwerty_enter);
			put(-10, R.drawable.key_qwerty_space);
			put(KEYCODE_QWERTY_BACKSPACE, R.drawable.key_qwerty_del);
			put(KEYCODE_JP12_ENTER, R.drawable.key_12key_enter);
			put(KEYCODE_JP12_SPACE, R.drawable.key_12key_space);
			put(KEYCODE_JP12_BACKSPACE, R.drawable.key_12key_del);
		}});
		put(1, new SparseArray<Integer>() {{
			put(KEYCODE_QWERTY_SHIFT, R.drawable.key_qwerty_shift_b);
			put(KEYCODE_QWERTY_ENTER, R.drawable.key_qwerty_enter_b);
			put(-10, R.drawable.key_qwerty_space_b);
			put(KEYCODE_QWERTY_BACKSPACE, R.drawable.key_qwerty_del_b);
			put(KEYCODE_JP12_ENTER, R.drawable.key_12key_enter_b);
			put(KEYCODE_JP12_SPACE, R.drawable.key_12key_space_b);
			put(KEYCODE_JP12_BACKSPACE, R.drawable.key_12key_del_b);
		}});
	}};
*/

	int mLongPressTimeout = 500;

	class LongClickHandler implements Runnable {
		int keyCode;
		boolean performed = false;
		public LongClickHandler(int keyCode) {
			this.keyCode = keyCode;
		}
		public void run() {
			setPreviewEnabled(keyCode);
			onKey(SoftKeyAction.PRESS, keyCode, SoftKeyPressType.LONG);
			try { vibrator.vibrate(vibrateDuration * 2); } catch (Exception ex) { }
			performed = true;
		}
	}

	Handler backspaceLongClickHandler = new Handler();
	class BackspaceLongClickHandler implements Runnable {
		@Override
		public void run() {
			Event.fire(DefaultSoftKeyboard.this, new DeleteCharEvent(1, 0));
			backspaceLongClickHandler.postDelayed(new BackspaceLongClickHandler(), 50);
		}
	}

	private SparseArray<TouchPoint> mTouchPoints = new SparseArray<>();
	class TouchPoint {
		Keyboard.Key key;
		int keyCode;

		float downX, downY;
		float dx, dy;
		float beforeX, beforeY;
		int space = -1;
		int spaceDistance;
		int backspace = -1;
		int backspaceDistance;

		LongClickHandler longClickHandler;
		Handler handler;

		SoftKeyEvent.SoftKeyPressType type;

		public TouchPoint(Keyboard.Key key, float downX, float downY) {
			this.key = key;
			this.keyCode = key.codes[0];
			this.downX = downX;
			this.downY = downY;
			handler = new Handler();
			handler.postDelayed(longClickHandler = new LongClickHandler(keyCode), mLongPressTimeout);

			key.onPressed();
			keyboardView.invalidateAllKeys();
			keyboardView.requestLayout();

			/* key click sound & vibration */
			if (vibrator != null) {
				try { vibrator.vibrate(vibrateDuration); } catch (Exception ex) { }
			}
			if (sound != null) {
				try { sound.seekTo(0); sound.start(); } catch (Exception ex) { }
			}
			this.type = SoftKeyEvent.SoftKeyPressType.SIGNLE;
			onKey(SoftKeyAction.PRESS, keyCode, type);
		}

		public boolean onMove(float x, float y) {
			SoftKeyEvent.SoftKeyPressType t = type;
			dx = x - downX;
			dy = y - downY;
			switch(keyCode) {
			case KeyEvent.KEYCODE_SPACE:	//TODO: Space
				if(Math.abs(dx) >= spaceSlideSensitivity) space = keyCode;
				break;

			case KeyEvent.KEYCODE_DEL:	//TODO: Backspace
				if(Math.abs(dx) >= BACKSPACE_SLIDE_UNIT) {
					backspace = keyCode;
					backspaceLongClickHandler.removeCallbacksAndMessages(null);
				}
				break;

			default:
				space = -1;
				backspace = -1;
				break;
			}
			if(dy > flickSensitivity || dy < -flickSensitivity
					|| dx < -flickSensitivity || dx > flickSensitivity || space != -1) {
				handler.removeCallbacksAndMessages(null);
			}
			if(space != -1) {
				spaceDistance += x - beforeX;
				if(spaceDistance < -SPACE_SLIDE_UNIT) {
					spaceDistance = 0;
					//TODO: dpad left
				}
				if(spaceDistance > +SPACE_SLIDE_UNIT) {
					spaceDistance = 0;
					//TODO: dpad right
				}
			}
			if(backspace != -1) {
				backspaceDistance += x - beforeX;
				if(backspaceDistance < -BACKSPACE_SLIDE_UNIT) {
					backspaceDistance = 0;
					//TODO: backspace left
				}
				if(backspaceDistance > +BACKSPACE_SLIDE_UNIT) {
					backspaceDistance = 0;
					//TODO: backspace right
				}
			}
			if(dy > flickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					//TODO: flick down
					type = SoftKeyEvent.SoftKeyPressType.FLICK_DOWN;
				}
				return false;
			}
			if(dy < -flickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					//TODO: flick up
					type = SoftKeyEvent.SoftKeyPressType.FLICK_UP;
				}
				return false;
			}
			if(dx < -flickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					//TODO: flick left
					type = SoftKeyEvent.SoftKeyPressType.FLICK_LEFT;
				}
				return false;
			}
			if(dx > flickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					//TODO: flick right
					type = SoftKeyEvent.SoftKeyPressType.FLICK_RIGHT;
				}
				return false;
			}

			if(dy > flickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					//TODO: flick down
					type = SoftKeyEvent.SoftKeyPressType.FLICK_DOWN;
				}
				return false;
			}
			if(dy < -flickSensitivity) {
				if(Math.abs(dy) > Math.abs(dx)) {
					//TODO: flick up
					type = SoftKeyEvent.SoftKeyPressType.FLICK_UP;
				}
				return false;
			}
			if(dx < -flickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					//TODO: flick left
					type = SoftKeyEvent.SoftKeyPressType.FLICK_LEFT;
				}
				return false;
			}
			if(dx > flickSensitivity) {
				if(Math.abs(dx) > Math.abs(dy)) {
					//TODO: flick right
					type = SoftKeyPressType.FLICK_RIGHT;
				}
				return false;
			}
			if(type != t) {
				onKey(SoftKeyAction.CANCEL, keyCode, t);
			}
			beforeX = x;
			beforeY = y;
			return true;
		}

		public boolean onUp() {

			key.onReleased(true);
			keyboardView.invalidateAllKeys();
			keyboardView.requestLayout();

			handler.removeCallbacksAndMessages(null);
			if(space != -1) {
				space = -1;
				return false;
			}
			if(backspace != -1) {
				//TODO: backspace commit
				backspace = -1;
				return false;
			}
			if(longClickHandler.performed && type == SoftKeyPressType.SIGNLE) type = SoftKeyPressType.LONG;
			onKey(SoftKeyAction.RELEASE, keyCode, type);
			return false;
		}

	}

	class OnKeyboardViewTouchListener implements View.OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(Build.VERSION.SDK_INT >= 8) {
				int pointerIndex = event.getActionIndex();
				int pointerId = event.getPointerId(pointerIndex);
				int action = event.getActionMasked();
				float x = event.getX(pointerIndex), y = event.getY(pointerIndex);
				switch(action) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					TouchPoint point = new TouchPoint(findKey(keyboard, (int) x, (int) y), x, y);
					mTouchPoints.put(pointerId, point);
					return true;

				case MotionEvent.ACTION_MOVE:
					return mTouchPoints.get(pointerId).onMove(x, y);

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					mTouchPoints.get(pointerId).onUp();
					mTouchPoints.remove(pointerId);
					return true;

				}
			} else {
				float x = event.getX(), y = event.getY();
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					TouchPoint point = new TouchPoint(findKey(keyboard, (int) x, (int) y), x, y);
					mTouchPoints.put(0, point);
					return true;

				case MotionEvent.ACTION_MOVE:
					return mTouchPoints.get(0).onMove(x, y);

				case MotionEvent.ACTION_UP:
					mTouchPoints.get(0).onUp();
					mTouchPoints.remove(0);
					return true;

				}
			}
			return false;
		}

		private Keyboard.Key findKey(Keyboard keyboard, int x, int y) {
			for(Keyboard.Key key : keyboard.getKeys()) {
				if(key.isInside(x, y)) return key;
			}
			return null;
		}

	}

	@Override
	public void init() {
	}

	@Override
	public View createView(Context context) {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String skin = pref.getString("keyboard_skin",
				context.getResources().getString(R.string.keyboard_skin_id_default));
		int id = context.getResources().getIdentifier(skin, "layout", "io.github.lee0701.heonot");

		LayoutInflater inflater = LayoutInflater.from(context);

		keyboardView = (KeyboardView) inflater.inflate(id, null);
		keyboardView.setOnKeyboardActionListener(this);

		mainView = (ViewGroup) inflater.inflate(R.layout.keyboard_default_main, null);
		subView = (ViewGroup) inflater.inflate(R.layout.keyboard_default_sub, null);

		mainView.addView(subView);
		mainView.addView(keyboardView);

		if(keyboardResName != null) {
			keyboardResId = context.getResources().getIdentifier(keyboardResName, "xml", context.getPackageName());
		}

		keyboard = new KeyboardKOKR(context, keyboardResId);
		updateLabels(keyboard, labels);

		keyboardView.setKeyboard(keyboard);

		keyboardView.setOnTouchListener(new OnKeyboardViewTouchListener());

		return mainView;
	}

	public void onKey(SoftKeyAction action, int primaryCode, SoftKeyPressType type) {
		if(!disableKeyInput) Event.fire(this, new SoftKeyEvent(action, primaryCode, type));
	}

	@Override
	public void onPress(int x) {
		setPreviewEnabled(x);
	}

	@Override
	public void onRelease(int x) {
		keyboardView.setPreviewEnabled(false);
		backspaceLongClickHandler.removeCallbacksAndMessages(null);
	}

	public void setPreviewEnabled(int x) {

	}

	@SuppressWarnings("deprecation")
	public Keyboard loadKeyboardLayout(Context context, int xmlLayoutResId) {
		KeyboardKOKR keyboard = new KeyboardKOKR(context, xmlLayoutResId);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int height = (displayMode == PORTRAIT) ? keyHeightPortrait : keyHeightLandscape;
		height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);
		keyboard.resize(height);
		/*
		SparseArray<Integer> keyIcons = mKeyIcons.get(preference.getKeyIcon());
		for(Keyboard.Key key : keyboard.getKeys()) {
			Integer keyIcon = keyIcons.get(key.codes[0]);
			if(keyIcon != null) {
				Drawable drawable = context.getResources().getDrawable(keyIcon);
				key.icon = drawable;
				key.iconPreview = drawable;
			}
		}
		*/

		return keyboard;
	}

	protected void 	updateLabels(Keyboard kbd, Map<Integer, String> labels) {
		if(!(kbd instanceof KeyboardKOKR)) return;
		if(labels == null) {
			return;
		}
		for(Keyboard.Key key : kbd.getKeys()) {
			String label = labels.get(key.codes[0]);
			if(label != null) {
				key.label = label;
			}
		}
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof SetPropertyEvent) {
			SetPropertyEvent event = (SetPropertyEvent) e;
			this.setProperty(event.getKey(), event.getValue());
		}
		if(e instanceof UpdateStateEvent) {
			if(labels != null) {
				this.updateLabels(keyboard, labels);
				keyboardView.invalidateAllKeys();
				keyboardView.requestLayout();
			}
		}
	}

	@Override
	public void setProperty(String key, Object value) {
		switch (key) {
		case "keyboard":
			if(value instanceof String) {
				keyboardResName = (String) value;
			}
			break;
		case "soft-key-labels":
			try {
				this.labels = (Map<Integer, String>) value;
			} catch(ClassCastException ex) {
				ex.printStackTrace();
			}
			break;
		}
	}

	@Override
	public JSONObject toJSONObject() throws JSONException {
		JSONObject object = super.toJSONObject();
		JSONArray properties = new JSONArray();

		JSONObject keyboard = new JSONObject();
		keyboard.put("key", "keyboard");
		keyboard.put("value", this.keyboardResName);
		properties.put(keyboard);

		object.put("properties", properties);

		return object;
	}

	public String getKeyboardResName() {
		return keyboardResName;
	}

	public void setKeyboardResName(String keyboardResName) {
		this.keyboardResName = keyboardResName;
	}

	@Override
	public Object clone() {
		DefaultSoftKeyboard cloned = new DefaultSoftKeyboard();
		cloned.setKeyboardResName(keyboardResName);
		cloned.setName(getName());
		return cloned;
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {

	}

	@Override
	public void onText(CharSequence text) {

	}

	@Override
	public void swipeLeft() {

	}

	@Override
	public void swipeRight() {

	}

	@Override
	public void swipeDown() {

	}

	@Override
	public void swipeUp() {

	}

}
