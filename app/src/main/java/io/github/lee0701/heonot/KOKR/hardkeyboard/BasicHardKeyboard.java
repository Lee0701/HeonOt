package io.github.lee0701.heonot.KOKR.hardkeyboard;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.HardKeyEvent;
import io.github.lee0701.heonot.KOKR.event.EventListener;

public class BasicHardKeyboard implements HardKeyboard {

	List<EventListener> listeners = new ArrayList<>();

	@Override
	public void init() {

	}

	@Override
	public void input(HardKeyEvent event) {

	}

	@Override
	public void onEvent(Event event) {

	}

	@Override
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public List<EventListener> getListeners() {
		return listeners;
	}
}
