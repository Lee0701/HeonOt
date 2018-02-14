package io.github.lee0701.heonot.KOKR.generator;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.event.CommitCharEvent;
import io.github.lee0701.heonot.KOKR.event.DeleteCharEvent;
import io.github.lee0701.heonot.KOKR.event.Event;
import io.github.lee0701.heonot.KOKR.event.InputCharEvent;
import io.github.lee0701.heonot.KOKR.event.EventListener;

public class EmptyCharacterGenerator implements CharacterGenerator {

	List<EventListener> listeners = new ArrayList<>();

	@Override
	public void init() {

	}

	@Override
	public void input(long code) {
		Event.fire(this, new CommitCharEvent((char) code, 1));
	}

	@Override
	public String testInput(long code) {
		return String.valueOf((char) code);
	}

	@Override
	public void backspace(int mode) {
		Event.fire(this, new DeleteCharEvent(1, 0));
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof InputCharEvent) {
			InputCharEvent event = (InputCharEvent) e;
			Object o = event.getCharacter();
			if(o instanceof Long) this.input((long) o);
			else if(o instanceof Integer) this.input((int) o);
		}
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
