package io.github.lee0701.heonot.KOKR.modules.hardkeyboard;

import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
import io.github.lee0701.heonot.KOKR.modules.InputMethodModule;

public abstract class HardKeyboard extends InputMethodModule {

	public abstract void input(HardKeyEvent event);

}
