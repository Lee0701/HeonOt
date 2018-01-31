package me.blog.hgl1002.openwnn.KOKR.hardkeyboard;

import java.util.ArrayList;
import java.util.List;

import me.blog.hgl1002.openwnn.KOKR.event.Event;
import me.blog.hgl1002.openwnn.KOKR.event.KeyPressEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;

public class BasicHardKeyboard implements HardKeyboard {

	List<Listener> listeners = new ArrayList<>();

	@Override
	public void init() {

	}

	@Override
	public void input(KeyPressEvent event) {

	}

	@Override
	public void onEvent(Event event) {

	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
