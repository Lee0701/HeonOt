package io.github.lee0701.heonot.inputmethod.modules.generator;

import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent;
import io.github.lee0701.heonot.inputmethod.event.DeleteCharEvent;
import io.github.lee0701.heonot.inputmethod.event.Event;
import io.github.lee0701.heonot.inputmethod.event.InputCharEvent;

public class EmptyCharacterGenerator extends CharacterGenerator {

	@Override
	public void init() {

	}

	@Override
	public void input(long code) {
		Event.fire(this, new CommitCharEvent((char) code, 1));
	}

	@Override
	public void backspace() {
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
		else if(e instanceof DeleteCharEvent) {
			Event.fire(this, e);
		}
	}

	@Override
	public Object clone() {
		EmptyCharacterGenerator cloned = new EmptyCharacterGenerator();
		cloned.setName(getName());
		return cloned;
	}
}
