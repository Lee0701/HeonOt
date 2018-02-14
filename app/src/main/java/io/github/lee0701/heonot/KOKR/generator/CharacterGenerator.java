package io.github.lee0701.heonot.KOKR.generator;

import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.EventSource;

public interface CharacterGenerator extends EventListener, EventSource {

	void init();

	void input(long code);

	String testInput(long code);

	void backspace(int mode);

}
