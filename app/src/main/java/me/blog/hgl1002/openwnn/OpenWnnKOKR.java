package me.blog.hgl1002.openwnn;

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
import java.util.List;

import me.blog.hgl1002.openwnn.KOKR.InputMethod;
import me.blog.hgl1002.openwnn.KOKR.KeystrokePreference;
import me.blog.hgl1002.openwnn.KOKR.event.*;
import me.blog.hgl1002.openwnn.KOKR.event.FinishComposingEvent;
import me.blog.hgl1002.openwnn.KOKR.generator.CharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.generator.EmptyCharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.generator.UnicodeCharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.DefaultHardKeyboard;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.HardKeyboard;
import me.blog.hgl1002.openwnn.KOKR.softkeyboard.SoftKeyboard;
import me.blog.hgl1002.openwnn.KOKR.softkeyboard.DefaultSoftKeyboard;

public class OpenWnnKOKR extends InputMethodService implements Listener {

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

	public static final int LONG_CLICK_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x100;
	public static final int FLICK_UP_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x101;
	public static final int FLICK_DOWN_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x102;
	public static final int FLICK_LEFT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x103;
	public static final int FLICK_RIGHT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x104;

	public static final int TIMEOUT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x1;

	public static final int BACKSPACE_LEFT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x211;
	public static final int BACKSPACE_RIGHT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x212;
	public static final int BACKSPACE_COMMIT_EVENT = OpenWnnEvent.PRIVATE_EVENT_OFFSET | 0x210;

	public static final String LANGKEY_SWITCH_KOR_ENG = "switch_kor_eng";
	public static final String LANGKEY_SWITCH_NEXT_METHOD = "switch_next_method";
	public static final String LANGKEY_LIST_METHODS = "list_methods";

	public static final String FLICK_NONE = "none";
	public static final String FLICK_SHIFT = "shift";
	public static final String FLICK_SYMBOL = "symbol";
	public static final String FLICK_SYMBOL_SHIFT = "symbol_shift";

	List<Listener> listeners = new ArrayList<>();

	List<InputMethod> mInputMethods;
	int mCurrentInputMethodId;
	InputMethod mCurrentInputMethod;

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

	KeystrokePreference.KeyStroke mHardLangKey;

	private static OpenWnnKOKR mSelf;
	public static OpenWnnKOKR getInstance() {
		return mSelf;
	}
	
	public OpenWnnKOKR() {
		super();
		mSelf = this;
		mInputMethods = new ArrayList<>();
	}
	
	public OpenWnnKOKR(Context context) {
		this();
		attachBaseContext(context);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		{
			SoftKeyboard softKeyboard = new DefaultSoftKeyboard(R.xml.keyboard_full_10cols);
			softKeyboard.addListener(this);
			HardKeyboard hardKeyboard = new DefaultHardKeyboard();
			hardKeyboard.addListener(this);
			CharacterGenerator characterGenerator = new EmptyCharacterGenerator();
			characterGenerator.addListener(this);
			hardKeyboard.addListener(characterGenerator);
			InputMethod qwerty = new InputMethod(softKeyboard, hardKeyboard, characterGenerator);
			mInputMethods.add(qwerty);
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
			SoftKeyboard softKeyboard = new DefaultSoftKeyboard(R.xml.keyboard_full_10cols);
			softKeyboard.addListener(this);
			HardKeyboard hardKeyboard = new DefaultHardKeyboard(str);
			hardKeyboard.addListener(this);
			CharacterGenerator characterGenerator = new UnicodeCharacterGenerator();
			characterGenerator.addListener(this);
			hardKeyboard.addListener(characterGenerator);
			InputMethod sebul391 = new InputMethod(softKeyboard, hardKeyboard, characterGenerator);
			mInputMethods.add(sebul391);
		}
		mCurrentInputMethod = mInputMethods.get(mCurrentInputMethodId);
	}

	@Override
	public View onCreateInputView() {
		for(InputMethod method : mInputMethods) {
			method.init();
		}
		int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
		boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
//		((DefaultSoftKeyboardKOKR) mInputViewManager).setHardKeyboardHidden(hidden);
		return mCurrentInputMethod.getSoftKeyboard().createView(this);
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
		mHardLangKey = KeystrokePreference.parseKeyStroke(pref.getString("system_hardware_lang_key_stroke", "---s62"));

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
		mCurrentInputMethod.getHardKeyboard().input(new KeyPressEvent(keyCode, event.getMetaState(), event.getRepeatCount()));
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		mCurrentInputMethod.getHardKeyboard().input(new KeyPressEvent.KeyReleaseEvent(keyCode, event.getMetaState(), event.getRepeatCount()));
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
		if(mCurrentInputMethod.getCharacterGenerator() instanceof UnicodeCharacterGenerator) {
			((UnicodeCharacterGenerator) mCurrentInputMethod.getCharacterGenerator()).finishComposing();
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
		else if(e instanceof SoftKeyPressEvent) {
			SoftKeyPressEvent event = (SoftKeyPressEvent) e;
			if(e instanceof SoftKeyPressEvent.SoftKeyReleaseEvent) {
				mCurrentInputMethod.getHardKeyboard().input(new KeyPressEvent(event.getKeyCode(), 0, 0));
			}
		}
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

}
