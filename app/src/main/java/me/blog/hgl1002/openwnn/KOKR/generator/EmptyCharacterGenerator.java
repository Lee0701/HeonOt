package me.blog.hgl1002.openwnn.KOKR.generator;

import java.util.ArrayList;
import java.util.List;

import me.blog.hgl1002.openwnn.KOKR.event.CommitCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.DeleteCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Event;
import me.blog.hgl1002.openwnn.KOKR.event.InputCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;

public class EmptyCharacterGenerator implements CharacterGenerator {

	List<Listener> listeners = new ArrayList<>();

	@Override
	public void init() {

	}

	@Override
	public void input(long code) {
		Event.fire(listeners, new CommitCharEvent((char) code, 1));
	}

	@Override
	public void backspace(int mode) {
		Event.fire(listeners, new DeleteCharEvent(1, 0));
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
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
