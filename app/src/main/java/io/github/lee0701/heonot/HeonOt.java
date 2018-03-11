package io.github.lee0701.heonot;

import android.content.Context;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import io.github.lee0701.heonot.inputmethod.InputMethod;
import io.github.lee0701.heonot.inputmethod.event.*;
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;
import io.github.lee0701.heonot.inputmethod.modules.global.ShortcutProcessor;
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.KeyStroke;
import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeBuilder;
import io.github.lee0701.heonot.inputmethod.scripting.TreeEvaluator;
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeonOt extends InputMethodService {

	private List<InputMethod> inputMethods;
	private int currentInputMethodId;
	private InputMethod currentInputMethod;

	private List<InputMethodModule> globalModules;

	private TreeEvaluator treeEvaluator;

	private static HeonOt instance;
	public static HeonOt getInstance() {
		return instance;
	}
	
	public HeonOt() {
		super();
		inputMethods = new ArrayList<>();
		globalModules = new ArrayList<>();
	}

	public HeonOt(Context context) {
		this();
		attachBaseContext(context);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		init();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroy();
	}

	void init() {
		treeEvaluator = new TreeEvaluator();

		File methodsDir = new File(getFilesDir(), "methods");

		if(!methodsDir.exists()) {
			methodsDir.mkdir();
			try {
				String method = getRawString("method_qwerty");
				inputMethods.add(InputMethod.loadJSON(method));
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			try {
				String method = getRawString("method_sebeol_391");
				inputMethods.add(InputMethod.loadJSON(method));
			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			InputMethodLoader.storeMethods(methodsDir, inputMethods);
		} else {
			inputMethods = InputMethodLoader.loadMethods(methodsDir);
		}

		ShortcutProcessor shortcutProcessor = new ShortcutProcessor();
		List<ShortcutProcessor.Shortcut> shortcuts = new ArrayList<>();
		{
			KeyStroke stroke = new KeyStroke(false, false, false, true, KeyEvent.KEYCODE_SPACE);
			TreeNode node = new StringRecursionTreeBuilder().build("A = !A");
			shortcuts.add(new ShortcutProcessor.Shortcut(stroke, ShortcutProcessor.Shortcut.MODE_CHANGE, node));
		}
		{
			KeyStroke stroke = new KeyStroke(false, false, false, false, KeyEvent.KEYCODE_LANGUAGE_SWITCH);
			TreeNode node = new StringRecursionTreeBuilder().build("A = !A");
			shortcuts.add(new ShortcutProcessor.Shortcut(stroke, ShortcutProcessor.Shortcut.MODE_CHANGE, node));
		}
		shortcutProcessor.setShortcuts(shortcuts);
		globalModules.add(shortcutProcessor);

		for(InputMethodModule module : globalModules) {
			EventBus.getDefault().register(module);
		}

		currentInputMethod = inputMethods.get(currentInputMethodId);
		currentInputMethod.registerListeners();
		EventBus.getDefault().register(this);
		currentInputMethod.init();
	}

	public void destroy() {
		EventBus.getDefault().unregister(this);
		for(InputMethod method : inputMethods) {
			method.pause();
		}
		for(InputMethodModule module : globalModules) {
			EventBus.getDefault().unregister(module);
		}
	}

	@Override
	public View onCreateInputView() {
		int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
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
				//TODO: Implement soft keyboard auto hiding.
			}
		} catch (Exception ex) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		if(isSystemKey(e.getKeyCode())) return false;
		HardKeyEvent event = new HardKeyEvent(e, HardKeyEvent.HardKeyAction.PRESS, keyCode, e.getMetaState(), e.getRepeatCount());
		EventBus.getDefault().post(event);
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent e) {
		if(isSystemKey(e.getKeyCode())) return false;
		HardKeyEvent event = new HardKeyEvent(e, HardKeyEvent.HardKeyAction.RELEASE, keyCode, e.getMetaState(), e.getRepeatCount());
		EventBus.getDefault().post(event);
		return true;
	}

	private boolean isSystemKey(int keyCode) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_VOLUME_UP:
			return true;
		}
		return false;
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
		EventBus.getDefault().post(new CommitComposingCharEvent());
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

	@Subscribe
	public void onComposeChar(ComposeCharEvent event) {
		String composing = event.getComposingChar();
		getCurrentInputConnection().setComposingText(composing, 1);
	}

	@Subscribe
	public void onFinishComposing(FinishComposingEvent event) {
		getCurrentInputConnection().finishComposingText();
	}

	@Subscribe
	public void onCommitChar(CommitCharEvent event) {
		InputConnection ic = getCurrentInputConnection();
		ic.finishComposingText();
		EventBus.getDefault().post(new CommitComposingCharEvent());
		ic.commitText(String.valueOf(event.getCharacter()), event.getCursorPosition());
	}

	@Subscribe
	public void onCommitString(CommitStringEvent event) {
		InputConnection ic = getCurrentInputConnection();
		ic.finishComposingText();
		EventBus.getDefault().post(new CommitComposingCharEvent());
		ic.commitText(event.getString(), event.getCursorPosition());
	}

	@Subscribe
	public void onBackspace(BackspaceEvent event) {
		getCurrentInputConnection().deleteSurroundingText(1, 0);
	}

	@Subscribe
	public void onDeleteChar(DeleteCharEvent event) {
		getCurrentInputConnection().deleteSurroundingText(event.getBeforeLength(), event.getAfterLength());
	}

	public Map<String, Long> getVariables() {
		return new HashMap<String, Long>() {{
			put("A", (long) currentInputMethodId);
		}};
	}

	public void changeInputMethod(int inputMethodId) {
		new Handler().post(() -> {
			currentInputMethod.pause();
			currentInputMethodId = inputMethodId;
			currentInputMethod = inputMethods.get(currentInputMethodId);
			currentInputMethod.registerListeners();
			currentInputMethod.init();
			setInputView(onCreateInputView());
		});
	}

	private String getRawString(String resName) throws IOException {
		InputStream is = getResources().openRawResource(getResources().getIdentifier(resName, "raw", getPackageName()));
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return new String(bytes);
	}

	public List<InputMethod> getInputMethods() {
		return inputMethods;
	}

	List<InputMethod> getInputMethodsCloned() {
		List<InputMethod> cloned = new ArrayList<>();
		for(InputMethod method : inputMethods) {
			cloned.add(new InputMethod(method));
		}
		return cloned;
	}

	void setInputMethods(List<InputMethod> inputMethods) {
		this.inputMethods = inputMethods;
	}

	public TreeEvaluator getTreeEvaluator() {
		return treeEvaluator;
	}

	public void setTreeEvaluator(TreeEvaluator treeEvaluator) {
		this.treeEvaluator = treeEvaluator;
	}
}
