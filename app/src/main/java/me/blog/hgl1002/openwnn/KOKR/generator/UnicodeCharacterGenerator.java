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
import me.blog.hgl1002.openwnn.KOKR.event.InputCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;

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
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable(charCode, (char) 0, (char) 0));
					state = states.pop();
				}
			} else {
				state.syllable.cho = charCode;
			}
			break;
		}

		case JUNG3: {
			if(state.syllable.containsJung()) {
				JamoPair pair = new JamoPair(state.syllable.jung, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jung = combination;
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable((char) 0, charCode, (char) 0));
					state = states.pop();
				}
			} else {
				state.syllable.jung = charCode;
			}
			break;
		}

		case JONG3: {
			if(state.syllable.containsJong()) {
				JamoPair pair = new JamoPair(state.syllable.jong, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jong = combination;
				} else {
					finishComposing();
					startNewSyllable(new UnicodeHangulSyllable((char) 0, (char) 0, charCode));
					state = states.pop();
				}
			} else {
				state.syllable.jong = charCode;
			}
			break;
		}

		default:
			finishComposing();
			fireEvent(new InputCharEvent(charCode, 1));
			state = states.pop();
		}

		state.composing = state.syllable.toString(true);

		fireEvent(new ComposeCharEvent(state.composing));

		states.push(state);
	}

	@Override
	public void backspace(int mode) {
		try {
			states.pop();
			State state = states.peek();
			fireEvent(new ComposeCharEvent(state.composing));
		} catch(EmptyStackException e) {
			fireEvent(new FinishComposingEvent());
			fireEvent(new DeleteCharEvent(1, 0));
		}
	}

	public void finishComposing() {
		fireEvent(new FinishComposingEvent());
		states.clear();
		states.push(new State());
	}

	public void startNewSyllable(UnicodeHangulSyllable syllable) {
		states.push(new State(syllable));
	}

	public static class State {
		UnicodeHangulSyllable syllable;
		char last, beforeJong;
		String composing;
		int lastInputType;

		public State(UnicodeHangulSyllable syllable) {
			this.syllable = syllable;
			last = beforeJong = 0;
			composing = "";
			lastInputType = 0;
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

	private void fireEvent(Event event) {
		for(Listener listener : listeners) {
			listener.onEvent(event);
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
