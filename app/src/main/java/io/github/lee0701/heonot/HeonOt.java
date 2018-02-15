package io.github.lee0701.heonot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lee0701.heonot.KOKR.InputMethod;
import io.github.lee0701.heonot.KOKR.event.CommitCharEvent;
import io.github.lee0701.heonot.KOKR.event.ComposeCharEvent;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.EventSource;
import io.github.lee0701.heonot.KOKR.event.CommitComposingCharEvent;
import io.github.lee0701.heonot.KOKR.event.FinishComposingEvent;
import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.ShortcutEvent;
import io.github.lee0701.heonot.KOKR.modules.hardkeyboard.HardKeyboard;
import io.github.lee0701.heonot.KOKR.modules.hardkeyboard.KeyStroke;
import io.github.lee0701.heonot.KOKR.scripting.StringRecursionTreeBuilder;
import io.github.lee0701.heonot.KOKR.scripting.TreeEvaluator;
import io.github.lee0701.heonot.KOKR.scripting.nodes.TreeNode;

public class HeonOt extends InputMethodService implements EventListener, EventSource {

	public static final String LANGKEY_SWITCH_KOR_ENG = "switch_kor_eng";
	public static final String LANGKEY_SWITCH_NEXT_METHOD = "switch_next_method";
	public static final String LANGKEY_LIST_METHODS = "list_methods";

	public static final String FLICK_NONE = "none";
	public static final String FLICK_SHIFT = "shift";
	public static final String FLICK_SYMBOL = "symbol";
	public static final String FLICK_SYMBOL_SHIFT = "symbol_shift";

	List<EventListener> listeners = new ArrayList<>();

	List<InputMethod> inputMethods;
	int currentInputMethodId;
	InputMethod currentInputMethod;

	boolean mMoachigi;
	boolean mHardwareMoachigi;
	boolean mFullMoachigi = true;
	int mMoachigiDelay;
	boolean mQuickPeriod;
	boolean mSpaceResetJohab;

	boolean mStandardJamo;
	String mLangKeyAction;
	String mLangKeyLongAction;

	String mFlickUpAction;
	String mFlickDownAction;
	String mFlickLeftAction;
	String mFlickRightAction;
	String mLongPressAction;

	boolean mAltDirect;

	boolean mSpace, mCharInput;
	boolean mInput;

	boolean mBackspaceSelectionMode;
	int mBackspaceSelectionStart;
	int mBackspaceSelectionEnd;

	Handler mTimeOutHandler;

	protected Map<KeyStroke, TreeNode> shortcuts;

	protected TreeEvaluator evaluator;

	private static HeonOt mSelf;
	public static HeonOt getInstance() {
		return mSelf;
	}
	
	public HeonOt() {
		super();
		mSelf = this;
		inputMethods = new ArrayList<>();
	}
	
	public HeonOt(Context context) {
		this();
		attachBaseContext(context);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		evaluator = new TreeEvaluator();
		{
			try {
				String method = getRawString("method_qwerty");
				inputMethods.add(InputMethod.load(method));
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		}
		{
			try {
				String method = getRawString("method_sebeol_391");
				inputMethods.add(InputMethod.load(method));
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
		}
		shortcuts = new HashMap<>();
		{
			KeyStroke stroke = new KeyStroke(false, false, false, true, KeyEvent.KEYCODE_SPACE);
			TreeNode node = new StringRecursionTreeBuilder().build("A = !A");
			shortcuts.put(stroke, node);
		}
		{
			KeyStroke stroke = new KeyStroke(false, false, false, false, KeyEvent.KEYCODE_LANGUAGE_SWITCH);
			TreeNode node = new StringRecursionTreeBuilder().build("A = !A");
			shortcuts.put(stroke, node);
		}

		currentInputMethod = inputMethods.get(currentInputMethodId);
		currentInputMethod.registerListeners(this);
		currentInputMethod.init();

	}

	@Override
	public View onCreateInputView() {
		int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
		return currentInputMethod.createView(this);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		commitComposingChar();
		if(restarting) {
			super.onStartInputView(attribute, restarting);
		} else {
			super.onStartInputView(attribute, restarting);

		}
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		mMoachigi = pref.getBoolean("keyboard_use_moachigi", mMoachigi);
		mHardwareMoachigi = pref.getBoolean("hardware_use_moachigi", mHardwareMoachigi);
		mFullMoachigi = pref.getBoolean("hardware_full_moachigi", mFullMoachigi);
		mMoachigiDelay = pref.getInt("hardware_full_moachigi_delay", 100);
		mQuickPeriod = pref.getBoolean("keyboard_quick_period", false);
		mSpaceResetJohab = pref.getBoolean("keyboard_space_reset_composing", false);

		mStandardJamo = pref.getBoolean("system_use_standard_jamo", mStandardJamo);
		mLangKeyAction = pref.getString("system_action_on_lang_key_press", LANGKEY_SWITCH_KOR_ENG);
		mLangKeyLongAction = pref.getString("system_action_on_lang_key_long_press", LANGKEY_LIST_METHODS);

		mFlickUpAction = pref.getString("keyboard_action_on_flick_up", FLICK_SHIFT);
		mFlickDownAction = pref.getString("keyboard_action_on_flick_down", FLICK_SYMBOL);
		mFlickLeftAction = pref.getString("keyboard_action_on_flick_left", FLICK_NONE);
		mFlickRightAction = pref.getString("keyboard_action_on_flick_right", FLICK_NONE);
		mLongPressAction = pref.getString("system_action_on_long_press", FLICK_SHIFT);

		//TODO: Implement this
//		if(hardKeyboardHidden) mQwertyEngine.setMoachigi(mMoachigi);
//		else mQwertyEngine.setMoachigi(mHardwareMoachigi);
//		mQwertyEngine.setFirstMidEnd(mStandardJamo);
//		m12keyEngine.setFirstMidEnd(mStandardJamo);

		mAltDirect = pref.getBoolean("hardware_alt_direct", true);


		mCharInput = false;

	}

	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
	}

	@Override
	public View onCreateCandidatesView() {
		return super.onCreateCandidatesView();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		try {
			super.onConfigurationChanged(newConfig);
			
			if (getCurrentInputConnection() != null) {
				int hiddenState = newConfig.hardKeyboardHidden;
				boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_VOLUME_UP:
			ShortcutEvent event = new ShortcutEvent(e.getKeyCode(), e.isAltPressed(), e.isShiftPressed());
			this.onEvent(event);
			return event.isCancelled();
		}
		HardKeyEvent event = new HardKeyEvent(HardKeyEvent.HardKeyAction.PRESS, keyCode, e.getMetaState(), e.getRepeatCount());
		Event.fire(this, event);
		return !event.isCancelled();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_VOLUME_UP:
			return false;
		}
		HardKeyEvent event = new HardKeyEvent(HardKeyEvent.HardKeyAction.RELEASE, keyCode, e.getMetaState(), e.getRepeatCount());
		Event.fire(this, event);
		return !event.isCancelled();
	}

	@Override
	public void onFinishInput() {
		super.onFinishInput();
	}

	@Override
	public void onViewClicked(boolean focusChanged) {
		super.onViewClicked(focusChanged);
		commitComposingChar();
	}

	private void commitComposingChar() {
		Event.fire(this, new CommitComposingCharEvent());
	}

	@Override
	public void hideWindow() {
		
		super.hideWindow();
	}

	@Override
	public boolean onEvaluateFullscreenMode() {
		return false;
	}

	@Override
	public boolean onEvaluateInputViewShown() {
		super.onEvaluateInputViewShown();
		return true;
	}

	@Override
	public void onEvent(Event e) {
		InputConnection ic = getCurrentInputConnection();
		if(e instanceof ComposeCharEvent) {
			ComposeCharEvent event = (ComposeCharEvent) e;
			String composing = event.getComposingChar();
			if(ic != null) ic.setComposingText(composing, 1);
		}
		else if(e instanceof FinishComposingEvent) {
			if(ic != null) ic.finishComposingText();
		}
		else if(e instanceof CommitCharEvent) {
			CommitCharEvent event = (CommitCharEvent) e;
			commitComposingChar();
			if(ic != null) ic.commitText(String.valueOf(event.getCharacter()), event.getCursorPosition());
		}
		else if(e instanceof DeleteCharEvent) {
			if(e.getSource() instanceof HardKeyboard) return;
			DeleteCharEvent event = (DeleteCharEvent) e;
			commitComposingChar();
			if(ic != null) ic.deleteSurroundingText(event.getBeforeLength(), event.getAfterLength());
		}
		else if(e instanceof ShortcutEvent) {
			ShortcutEvent event = (ShortcutEvent) e;
			for(KeyStroke stroke : shortcuts.keySet()) {
				if(stroke.getKeyCode() == event.getKeyCode()
						&& stroke.isAlt() == event.isAltPressed()
						&& stroke.isShift() == event.isShiftPressed()) {
					evaluator.setVariables(getVariables());
					Long result = evaluator.eval(shortcuts.get(stroke));
					setVariables(evaluator.getVariables());
					e.setCancelled(true);
				}
			}
		}
	}

	public Map<String, Long> getVariables() {
		return new HashMap<String, Long>() {{
			put("A", (long) currentInputMethodId);
		}};
	}

	public void setVariables(Map<String, Long> variables) {
		try {
			final int inputMethodId = (int) (long) variables.get("A");
			if(inputMethodId != currentInputMethodId) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						currentInputMethod.clearListeners();
						HeonOt.this.clearListeners();
						currentInputMethodId = inputMethodId;
						currentInputMethod = inputMethods.get(currentInputMethodId);
						currentInputMethod.registerListeners(HeonOt.this);
						currentInputMethod.init();
						setInputView(onCreateInputView());
					}
				});
			}
		} catch(NullPointerException e) {}
	}

	public String getRawString(String resName) throws IOException {
		InputStream is = getResources().openRawResource(getResources().getIdentifier(resName, "raw", getPackageName()));
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return new String(bytes);
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
	public void clearListeners() {
		listeners.clear();
	}

	@Override
	public List<EventListener> getListeners() {
		return listeners;
	}

}
