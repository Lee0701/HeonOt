package io.github.lee0701.heonot.inputmethod.modules.softkeyboard;

import android.content.Context;
import android.view.View;

import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;

public abstract class SoftKeyboard extends InputMethodModule{

	public abstract View createView(Context context);

}
