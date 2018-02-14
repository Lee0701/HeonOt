package io.github.lee0701.heonot.KOKR.hardkeyboard;

import io.github.lee0701.heonot.KOKR.event.EventSource;
import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
import io.github.lee0701.heonot.KOKR.event.EventListener;

public interface HardKeyboard extends EventListener, EventSource {

	void init();

	void input(HardKeyEvent event);

}
