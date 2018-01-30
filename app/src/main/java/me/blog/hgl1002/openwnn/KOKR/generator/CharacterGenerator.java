package me.blog.hgl1002.openwnn.KOKR.generator;

import me.blog.hgl1002.openwnn.KOKR.event.Listener;

public interface CharacterGenerator extends Listener {

	void init();

	void input(long code);

	void backspace(int mode);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
