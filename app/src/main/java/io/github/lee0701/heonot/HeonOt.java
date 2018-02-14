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
import io.github.lee0701.heonot.KOKR.event.FinishComposingEvent;
import io.github.lee0701.heonot.KOKR.event.KeyPressEvent;
import io.github.lee0701.heonot.KOKR.event.Listener;
import io.github.lee0701.heonot.KOKR.event.ShortcutRequestEvent;
import io.github.lee0701.heonot.KOKR.generator.EmptyCharacterGenerator;
import io.github.lee0701.heonot.KOKR.generator.UnicodeCharacterGenerator;
import io.github.lee0701.heonot.KOKR.hardkeyboard.DefaultHardKeyboard;
import io.github.lee0701.heonot.KOKR.generator.CharacterGenerator;
import io.github.lee0701.heonot.KOKR.hardkeyboard.HardKeyboard;
import io.github.lee0701.heonot.KOKR.hardkeyboard.KeyStroke;
import io.github.lee0701.heonot.KOKR.scripting.StringRecursionTreeBuilder;
import io.github.lee0701.heonot.KOKR.scripting.TreeEvaluator;
import io.github.lee0701.heonot.KOKR.scripting.nodes.TreeNode;
import io.github.lee0701.heonot.KOKR.softkeyboard.SoftKeyboard;
import io.github.lee0701.heonot.R;

public class HeonOt extends InputMethodService implements Listener {

	public static final int[][] SHIFT_CONVERT = {
			{0x60, 0x7e},
			{0x31, 0x21},
			{0x32, 0x40},
			{0x33, 0x23},
			{0x34, 0x24},
			{0x35, 0x25},
			{0x36, 0x5e},
			{0x37, 0x26},
			{0x38, 0x2a},
			{0x39, 0x28},
			{0x30, 0x29},
			{0x2d, 0x5f},
			{0x3d, 0x2b},
			
			{0x5b, 0x7b},
			{0x5d, 0x7d},
			{0x5c, 0x7c},

			{0x3b, 0x3a},
			{0x27, 0x22},
			
			{0x2c, 0x3c},
			{0x2e, 0x3e},
			{0x2f, 0x3f},
	};

	public static final int[][] FLICK_TABLE_12KEY = {
			{-201, 0x31},
			{-202, 0x32},
			{-203, 0x33},
			{-204, 0x34},
			{-205, 0x35},
			{-206, 0x36},
			{-207, 0x37},
			{-208, 0x38},
			{-209, 0x39},
			{-213, 0x2c},
			{-210, 0x30},
			{-211, 0x21},
	};

	public static final String LANGKEY_SWITCH_KOR_ENG = "switch_kor_eng";
	public static final String LANGKEY_SWITCH_NEXT_METHOD = "switch_next_method";
	public static final String LANGKEY_LIST_METHODS = "list_methods";

	public static final String FLICK_NONE = "none";
	public static final String FLICK_SHIFT = "shift";
	public static final String FLICK_SYMBOL = "symbol";
	public static final String FLICK_SYMBOL_SHIFT = "symbol_shift";

	List<Listener> listeners = new ArrayList<>();

	List<InputMethod> inputMethods;
	int currentInputMethodId;
	InputMethod currentInputMethod;

	boolean consumeDownEvent;

	int[][] mAltSymbols;

	boolean mDirectInputMode;
	boolean mEnableTimeout;
	
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
			SoftKeyboard softKeyboard = new io.github.lee0701.heonot.KOKR.softkeyboard.DefaultSoftKeyboard(R.xml.keyboard_full_10cols);
			HardKeyboard hardKeyboard = new DefaultHardKeyboard();
			CharacterGenerator characterGenerator = new EmptyCharacterGenerator();
			InputMethod qwerty = new InputMethod(this, softKeyboard, hardKeyboard, characterGenerator);
			inputMethods.add(qwerty);
		}
		{
			String str = "";
			try {
				InputStream is = getResources().openRawResource(R.raw.keyboard_sebul_391);
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				str = new String(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			SoftKeyboard softKeyboard = new io.github.lee0701.heonot.KOKR.softkeyboard.DefaultSoftKeyboard(R.xml.keyboard_full_10cols);
			HardKeyboard hardKeyboard = new DefaultHardKeyboard(str);
			CharacterGenerator characterGenerator = new UnicodeCharacterGenerator();
			InputMethod sebul391 = new InputMethod(this, softKeyboard, hardKeyboard, characterGenerator);
			inputMethods.add(sebul391);
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
	}

	@Override
	public View onCreateInputView() {
		for(InputMethod method : inputMethods) {
			method.init();
		}
		int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
//		((DefaultSoftKeyboardKOKR) mInputViewManager).setHardKeyboardHidden(hidden);
		return currentInputMethod.getSoftKeyboard().createView(this);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		finishComposing();
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
//				((DefaultSoftKeyboardKOKR) mInputViewManager).setHardKeyboardHidden(hidden);
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		currentInputMethod.getHardKeyboard().input(new KeyPressEvent(keyCode, event.getMetaState(), event.getRepeatCount()));
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		currentInputMethod.getHardKeyboard().input(new KeyPressEvent.KeyReleaseEvent(keyCode, event.getMetaState(), event.getRepeatCount()));
		return false;
	}

	@Override
	public void onFinishInput() {
		finishComposing();
		super.onFinishInput();
	}

	@Override
	public void onViewClicked(boolean focusChanged) {
		getCurrentInputConnection().finishComposingText();
		super.onViewClicked(focusChanged);
		finishComposing();
	}

	private void finishComposing() {
		if(currentInputMethod.getCharacterGenerator() instanceof UnicodeCharacterGenerator) {
			((UnicodeCharacterGenerator) currentInputMethod.getCharacterGenerator()).finishComposing();
		}
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
		if(e instanceof ComposeCharEvent) {
			ComposeCharEvent event = (ComposeCharEvent) e;
			String composing = event.getComposingChar();
			getCurrentInputConnection().setComposingText(composing, 1);
		}
		else if(e instanceof FinishComposingEvent) {
			getCurrentInputConnection().finishComposingText();
		}
		else if(e instanceof CommitCharEvent) {
			CommitCharEvent event = (CommitCharEvent) e;
			finishComposing();
			getCurrentInputConnection().commitText(String.valueOf(event.getCharacter()), event.getCursorPosition());
		}
		else if(e instanceof DeleteCharEvent) {
			DeleteCharEvent event = (DeleteCharEvent) e;
			finishComposing();
			getCurrentInputConnection().deleteSurroundingText(event.getBeforeLength(), event.getAfterLength());
		}
		else if(e instanceof ShortcutRequestEvent) {
			ShortcutRequestEvent event = (ShortcutRequestEvent) e;
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
			int inputMethodId = (int) (long) variables.get("A");
			if(inputMethodId != currentInputMethodId) {
				currentInputMethodId = inputMethodId;
				currentInputMethod = inputMethods.get(currentInputMethodId);
				setInputView(onCreateInputView());
			}
		} catch(NullPointerException e) {}
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

}
