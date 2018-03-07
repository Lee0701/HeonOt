package io.github.lee0701.heonot.inputmethod.modules.hardkeyboard;

import org.greenrobot.eventbus.Subscribe;

import io.github.lee0701.heonot.inputmethod.event.Event;
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent;

public class BasicHardKeyboard extends HardKeyboard {

	@Override
	public void init() {

	}

	@Override
	public void input(HardKeyEvent event) {

	}

	@Override
	public Object clone() {
		return new BasicHardKeyboard();
	}
}
