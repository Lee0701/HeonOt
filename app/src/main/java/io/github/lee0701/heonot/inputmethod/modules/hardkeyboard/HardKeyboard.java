package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard;

import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent;
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;

public abstract class HardKeyboard extends InputMethodModule {

	public abstract void input(HardKeyEvent event);

}
