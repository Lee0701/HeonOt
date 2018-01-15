package me.blog.hgl1002.openwnn.KOKR.generator;

import java.util.Map;
import java.util.Stack;

import static me.blog.hgl1002.openwnn.KOKR.generator.UnicodeJamoHandler.JamoPair;

public class UnicodeCharacterGenerator implements CharacterGenerator {

	Stack<State> states;

	Map<UnicodeJamoHandler.JamoPair, Character> combinationTable;

	boolean moachigi, firstMidEnd;

	@Override
	public void init() {
	    //Push initial placeholder state into the stack.
		states.push(new State());
	}

	@Override
	public void input(long code) {
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
                    }
                } else {
                    state.syllable.cho = charCode;
                }
                break;
            }

            case JUNG3: {
                break;
            }

            case JONG3: {
                break;
            }

		}

		state.composing = state.syllable.toString(firstMidEnd);

		states.push(state);
	}

	@Override
	public void backspace(int mode) {

	}

	public void finishComposing() {

	}

	public void startNewSyllable(UnicodeHangulSyllable syllable) {
		states.peek().syllable = syllable;
	}

	public static class State {
		UnicodeHangulSyllable syllable;
		char last, beforeJong;
		String composing;
		int lastInputType;

		public State() {
			this.syllable = new UnicodeHangulSyllable();
			last = beforeJong = 0;
			composing = "";
			lastInputType = 0;
		}

		public State(State previousState) {
			syllable = (UnicodeHangulSyllable) previousState.syllable.clone();
			beforeJong = previousState.beforeJong;
			composing = previousState.composing;
		}

	}

}
