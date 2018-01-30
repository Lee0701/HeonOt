package me.blog.hgl1002.openwnn.KOKR.hardkeyboard;

import me.blog.hgl1002.openwnn.KOKR.event.KeyPressEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;

public interface HardKeyboard extends Listener {

	void init();

	void input(KeyPressEvent event);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
