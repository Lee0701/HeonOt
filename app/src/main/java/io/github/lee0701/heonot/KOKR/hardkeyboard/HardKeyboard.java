package io.github.lee0701.heonot.KOKR.hardkeyboard;

import io.github.lee0701.heonot.KOKR.event.KeyPressEvent;
import io.github.lee0701.heonot.KOKR.event.Listener;

public interface HardKeyboard extends Listener {

	void init();

	void input(KeyPressEvent event);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
