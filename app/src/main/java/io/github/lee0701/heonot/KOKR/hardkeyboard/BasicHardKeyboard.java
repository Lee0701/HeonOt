package io.github.lee0701.heonot.KOKR.hardkeyboard;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.KeyPressEvent;
import io.github.lee0701.heonot.KOKR.event.Listener;

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
