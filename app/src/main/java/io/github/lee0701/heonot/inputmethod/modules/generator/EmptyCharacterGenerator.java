package io.github.lee0701.heonot.inputmethod.modules.generator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent;
import io.github.lee0701.heonot.inputmethod.event.DeleteCharEvent;
import io.github.lee0701.heonot.inputmethod.event.Event;
import io.github.lee0701.heonot.inputmethod.event.InputCharEvent;

public class EmptyCharacterGenerator extends CharacterGenerator {

	@Override
	public void init() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void input(long code) {
		EventBus.getDefault().post(new CommitCharEvent((char) code, 1));
	}

	@Override
	public void backspace() {
	}

	@Subscribe
	public void onInputChar(InputCharEvent event) {
		Object o = event.getCharacter();
		if(o instanceof Long) this.input((long) o);
		else if(o instanceof Integer) this.input((int) o);
	}

	@Override
	public Object clone() {
		EmptyCharacterGenerator cloned = new EmptyCharacterGenerator();
		cloned.setName(getName());
		return cloned;
	}
}
