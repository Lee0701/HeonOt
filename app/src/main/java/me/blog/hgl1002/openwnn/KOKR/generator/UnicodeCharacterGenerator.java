package me.blog.hgl1002.openwnn.KOKR.generator;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import me.blog.hgl1002.openwnn.KOKR.event.ComposeCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.DeleteCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Event;
import me.blog.hgl1002.openwnn.KOKR.event.FinishComposingEvent;
import me.blog.hgl1002.openwnn.KOKR.event.CommitCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.InputCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;
import me.blog.hgl1002.openwnn.KOKR.event.SetPropertyEvent;

import static me.blog.hgl1002.openwnn.KOKR.generator.UnicodeJamoHandler.JamoPair;

public class UnicodeCharacterGenerator implements CharacterGenerator {

	List<Listener> listeners = new ArrayList<>();

	Stack<State> states = new Stack<>();

	Map<UnicodeJamoHandler.JamoPair, Character> combinationTable = new HashMap<>();

	boolean moachigi, firstMidEnd;

	@Override
	public void init() {
		//Push initial placeholder state into the stack.
		states.push(new State());
	}

	@Override
	public void input(long code) {
		if(states.empty()) states.push(new State());
		State state = new State(states.peek());

		char charCode = (char) (code & 0xffff);
		switch(UnicodeJamoHandler.getType(charCode)) {
		case CHO3: {
			if(state.syllable.containsCho()) {
				JamoPair pair = new JamoPair(state.syllable.cho, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.cho = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable(charCode, (char) 0, (char) 0));
					state = states.pop();
					state.lastInput |= State.INPUT_COMBINATION_FAILED | State.INPUT_NEW_SYLLABLE_STARTED;
				}
			} else {
				state.syllable.cho = charCode;
			}
			state.lastInput |= State.INPUT_CHO3;
			break;
		}

		case JUNG3: {
			if(state.syllable.containsJung()) {
				JamoPair pair = new JamoPair(state.syllable.jung, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jung = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable((char) 0, charCode, (char) 0));
					state = states.pop();
					state.lastInput |= State.INPUT_COMBINATION_FAILED | State.INPUT_NEW_SYLLABLE_STARTED;
				}
			} else {
				state.syllable.jung = charCode;
			}
			state.lastInput |= State.INPUT_JUNG3;
			break;
		}

		case JONG3: {
			if(state.syllable.containsJong()) {
				JamoPair pair = new JamoPair(state.syllable.jong, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jong = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable((char) 0, (char) 0, charCode));
					state = states.pop();
					state.lastInput |= State.INPUT_COMBINATION_FAILED | State.INPUT_NEW_SYLLABLE_STARTED;
				}
			} else {
				state.syllable.jong = charCode;
			}
			state.lastInput |= State.INPUT_JONG3;
			break;
		}

		default:
			finishComposing();
			Event.fire(listeners, new CommitCharEvent(charCode, 1));
			state = states.pop();
			state.lastInput = State.INPUT_NON_HANGUL;
		}

		state.composing = state.syllable.toString(true);

		Event.fire(listeners, new ComposeCharEvent(state.composing, state.lastInput));

		states.push(state);
	}

	@Override
	public void backspace(int mode) {
		try {
			states.pop();
			State state = states.peek();
			Event.fire(listeners, new ComposeCharEvent(state.composing, state.lastInput));
		} catch(EmptyStackException e) {
			Event.fire(listeners, new FinishComposingEvent());
			Event.fire(listeners, new DeleteCharEvent(1, 0));
		}
	}

	public void finishComposing() {
		Event.fire(listeners, new FinishComposingEvent());
		states.clear();
		states.push(new State());
	}

	public void startNewSyllable(UnicodeHangulSyllable syllable) {
		states.push(new State(syllable));
	}

	public static class State {

		public static final int MASK_HANGUL_BEOL = 0xf000;
		public static final int MASK_HANGUL_TYPE = 0x0f00;

		public static final int INPUT_NON_HANGUL = 0x0000;

		public static final int INPUT_DUBEOL = 0x2000;
		public static final int INPUT_SEBEOL = 0x3000;

		public static final int INPUT_CHO = 0x0100;
		public static final int INPUT_JUNG = 0x0200;
		public static final int INPUT_JONG = 0x0300;

		public static final int INPUT_CHO3 = 0x3100;
		public static final int INPUT_JUNG3 = 0x3200;
		public static final int INPUT_JONG3 = 0x3300;

		public static final int INPUT_CHO2 = 0x2100;
		public static final int INPUT_JUNG2 = 0x2200;
		public static final int INPUT_JONG2 = 0x2300;

		public static final int INPUT_COMBINED = 0x0001;
		public static final int INPUT_COMBINATION_FAILED = 0x0002;
		public static final int INPUT_NEW_SYLLABLE_STARTED = 0x0004;

		UnicodeHangulSyllable syllable;
		char last, beforeJong;
		String composing;
		int lastInput;

		public State(UnicodeHangulSyllable syllable) {
			this.syllable = syllable;
			last = beforeJong = 0;
			composing = "";
			lastInput = 0;
		}

		public State() {
			this(new UnicodeHangulSyllable());
		}

		public State(State previousState) {
			syllable = (UnicodeHangulSyllable) previousState.syllable.clone();
			beforeJong = previousState.beforeJong;
			composing = previousState.composing;
		}

	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof InputCharEvent) {
			InputCharEvent event = (InputCharEvent) e;
			Object o = event.getCharacter();
			if(o instanceof Long) this.input((long) o);
			else if(o instanceof Integer) this.input((int) o);
		}
		if(e instanceof SetPropertyEvent) {
			SetPropertyEvent event = (SetPropertyEvent) e;
			this.setProperty(event.getKey(), event.getValue());
		}
	}

	public void setProperty(String key, Object value) {
		switch(key) {
		case "combination-table":
			try {
				this.combinationTable = (Map<JamoPair, Character>) value;
			} catch(ClassCastException ex) {
				ex.printStackTrace();
			}
			break;
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
