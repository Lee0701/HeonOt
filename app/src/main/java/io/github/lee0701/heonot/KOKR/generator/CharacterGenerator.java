package io.github.lee0701.heonot.KOKR.generator;

import io.github.lee0701.heonot.KOKR.event.Listener;

public interface CharacterGenerator extends Listener {

	void init();

	void input(long code);

	String testInput(long code);

	void backspace(int mode);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
