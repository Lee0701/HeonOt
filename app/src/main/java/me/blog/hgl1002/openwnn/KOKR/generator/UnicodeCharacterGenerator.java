package me.blog.hgl1002.openwnn.KOKR.generator;

import java.util.Stack;

public class UnicodeCharacterGenerator implements CharacterGenerator {

	Stack<State> states;

	@Override
	public void init() {
		State initialState = new State();
		states.push(initialState);
	}

	@Override
	public void input(long code) {
		State state = new State(states.peek());

		states.push(state);
	}

	@Override
	public void backspace(int mode) {

	}

	public static class State {

		int cho, jung, jong;
		int last, beforeJong;
		String composing;
		int lastInputType;

		public State() {
			cho = jung = jong = -1;
			last = beforeJong = -1;
			composing = "";
			lastInputType = 0;
		}

		public State(State previousState) {
			cho = previousState.cho;
			jung = previousState.jung;
			jong = previousState.jong;
			beforeJong = previousState.beforeJong;
			composing = previousState.composing;
		}

	}

}
