package io.github.lee0701.heonot.inputmethod.modules.generator;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import io.github.lee0701.heonot.R;
import io.github.lee0701.heonot.inputmethod.event.*;
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.HardKeyboard;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.JamoPair;

public class UnicodeCharacterGenerator extends CharacterGenerator {

	private Stack<State> states = new Stack<>();

	private Map<UnicodeJamoHandler.JamoPair, Character> combinationTable = new HashMap<>();

	private boolean moajugi;
	private boolean fullMoachigi;
	private boolean firstMidEnd;

	@Override
	public void init() {
		//Push initial placeholder state into the stack.
		states.push(new State());
	}

	@Override
	public void pause() {

	}

	@Override
	public void input(long code) {
		State state = this.processInput(code);
		EventBus.getDefault().post(new ComposeCharEvent(state.composing, state.lastInput));
		states.push(state);
	}

	private State processInput(long code) {
		if(states.empty()) states.push(new State());
		State state = new State(states.peek());

		char charCode = (char) (code & 0xffff);
		switch(UnicodeJamoHandler.getType(charCode)) {
		case CHO3: {
			if(!fullMoachigi) {
				if(!moajugi) {
					if((state.lastInput & State.MASK_HANGUL_TYPE) != State.INPUT_CHO) {
						commitComposingChar();
						state = new State(states.peek());
					}
				} else {
					if((state.syllable.containsJung() || state.syllable.containsJong()) && state.syllable.containsCho()) {
						commitComposingChar();
						state = new State(states.peek());
					}
				}
			}
			state.lastInput = 0;
			if(state.syllable.containsCho()) {
				JamoPair pair = new JamoPair(state.syllable.cho, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.cho = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					commitComposingChar();
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
			if(!moajugi && !fullMoachigi) {
				switch(state.lastInput & State.MASK_HANGUL_TYPE) {
				case State.INPUT_CHO:
				case State.INPUT_JUNG:
					break;

				default:
					commitComposingChar();
					state = new State(states.peek());
					break;
				}
			}
			state.lastInput = 0;
			if(state.syllable.containsJung()) {
				JamoPair pair = new JamoPair(state.syllable.jung, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jung = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					commitComposingChar();
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
			if(!moajugi && !fullMoachigi) {
				switch(state.lastInput & State.MASK_HANGUL_TYPE) {
				case State.INPUT_JUNG:
				case State.INPUT_JONG:
					break;

				default:
					commitComposingChar();
					state = new State(states.peek());
					break;
				}
			}
			state.lastInput = 0;
			if(state.syllable.containsJong()) {
				JamoPair pair = new JamoPair(state.syllable.jong, charCode);
				Character combination = combinationTable.get(pair);
				if(combination != null) {
					state.syllable.jong = combination;
					state.lastInput |= State.INPUT_COMBINED;
				} else {
					commitComposingChar();
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
			commitComposingChar();
			EventBus.getDefault().post(new CommitCharEvent(charCode, 1));
			state = states.pop();
			state.lastInput = State.INPUT_NON_HANGUL;
		}

		state.composing = state.syllable.toString(getFirstMidEnd());

		return state;
	}

	@Override
	public void backspace() {
		try {
			states.pop();
			State state = states.peek();
			EventBus.getDefault().post(new ComposeCharEvent(state.composing, state.lastInput));
		} catch(EmptyStackException e) {
			EventBus.getDefault().post(new FinishComposingEvent());
			EventBus.getDefault().post(new DeleteCharEvent(1, 0));
		}
	}

	public void commitComposingChar() {
		EventBus.getDefault().post(new FinishComposingEvent());
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

		State(UnicodeHangulSyllable syllable) {
			this.syllable = syllable;
			last = beforeJong = 0;
			composing = "";
			lastInput = 0;
		}

		State() {
			this(new UnicodeHangulSyllable());
		}

		State(State previousState) {
			syllable = (UnicodeHangulSyllable) previousState.syllable.clone();
			beforeJong = previousState.beforeJong;
			composing = previousState.composing;
			lastInput = previousState.lastInput;
		}

	}

	@Subscribe
	public void onInputChar(InputCharEvent event) {
		Object o = event.getCharacter();
		if(o instanceof Long) this.input((long) o);
		else if(o instanceof Integer) this.input((int) o);
	}

	@Subscribe
	public void onCommitComposingChar(CommitComposingCharEvent event) {
		commitComposingChar();
	}

	@Subscribe(priority = 1)
	public void onBackspace(BackspaceEvent event) {
		this.backspace();
		EventBus.getDefault().cancelEventDelivery(event);
	}

	@Override
	public void setProperty(String key, Object value) {
		switch(key) {
		case "combination-table":
			try {
				if(value instanceof Map) {
					this.combinationTable = (Map<JamoPair, Character>) value;
				} else if(value instanceof JSONObject) {
					this.combinationTable = loadCombinationTable((JSONObject) value);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			break;
		case "moajugi":
			try {
				this.moajugi = (Boolean) value;
			} catch(ClassCastException | NullPointerException ex) {
				ex.printStackTrace();
			}
			break;

		case "first-mid-end":
			try {
				this.firstMidEnd = (Boolean) value;
			} catch(ClassCastException | NullPointerException ex) {
				ex.printStackTrace();
			}
			break;

		case "full-moachigi":
			try {
				this.fullMoachigi = (Boolean) value;
			} catch(ClassCastException | NullPointerException ex) {
				ex.printStackTrace();
			}
			break;
		}
	}

	@Override
	public JSONObject toJSONObject() throws JSONException {
		JSONObject object = super.toJSONObject();
		JSONObject properties = new JSONObject();

		properties.put("combination-table", this.storeCombinationTable());

		properties.put("moajugi", this.moajugi);
		properties.put("first-mid-end", this.firstMidEnd);
		properties.put("full-moachigi", this.fullMoachigi);

		object.put("properties", properties);

		return object;
	}

	public JSONObject storeCombinationTable() throws JSONException {
		JSONObject object = new JSONObject();
		JSONArray combination = new JSONArray();
		for(JamoPair pair : this.combinationTable.keySet()) {
			Character result = this.combinationTable.get(pair);
			JSONObject entry = new JSONObject();
			entry.put("a", pair.a);
			entry.put("b", pair.b);
			entry.put("result", Integer.toString(result));
			combination.put(entry);
		}
		object.put("combination", combination);
		return object;
	}

	public Map<JamoPair, Character> getCombinationTable() {
		return combinationTable;
	}

	public void setCombinationTable(Map<JamoPair, Character> combinationTable) {
		this.combinationTable = combinationTable;
	}

	public static Map<JamoPair, Character> loadCombinationTable(String combJson) throws JSONException {
		return loadCombinationTable(new JSONObject(combJson));
	}

	public static Map<JamoPair, Character> loadCombinationTable(JSONObject jsonObject) throws JSONException {
		Map<JamoPair, Character> combinationTable = new HashMap<>();

		JSONArray combination = jsonObject.getJSONArray("combination");
		if(combination != null) {
			for(int i = 0 ; i < combination.length() ; i++) {
				JSONObject o = combination.getJSONObject(i);
				int a = o.getInt("a");
				int b = o.getInt("b");
				String result = o.getString("result");
				JamoPair pair = new JamoPair((char) a, (char) b);
				combinationTable.put(pair, (char) Integer.parseInt(result));
			}
		}

		return combinationTable;
	}

	public boolean getMoajugi() {
		return moajugi;
	}

	public void setMoajugi(boolean moajugi) {
		this.moajugi = moajugi;
	}

	public boolean getFullMoachigi() {
		return fullMoachigi;
	}

	public void setFullMoachigi(boolean fullMoachigi) {
		this.fullMoachigi = fullMoachigi;
	}

	public boolean getFirstMidEnd() {
		return firstMidEnd;
	}

	public void setFirstMidEnd(boolean firstMidEnd) {
		this.firstMidEnd = firstMidEnd;
	}

	@Override
	public Object clone() {
		UnicodeCharacterGenerator cloned = new UnicodeCharacterGenerator();
		Map<JamoPair, Character> combinations = new HashMap<>();
		if(combinationTable != null) {
			for(JamoPair key : this.combinationTable.keySet()) {
				combinations.put((JamoPair) key.clone(), combinationTable.get(key));
			}
			cloned.setCombinationTable(combinations);
		}
		cloned.setMoajugi(getMoajugi());
		cloned.setFullMoachigi(getFullMoachigi());
		cloned.setFirstMidEnd(getFirstMidEnd());
		cloned.setName(getName());
		return cloned;
	}

	@Override
	public View createSettingsView(Context context) {
		LinearLayout settings = new LinearLayout(context);
		settings.setOrientation(LinearLayout.VERTICAL);
		CheckBox moajugi = new CheckBox(context);
		moajugi.setText(R.string.moajugi);
		CheckBox fullMoachigi = new CheckBox(context);
		fullMoachigi.setText(R.string.full_moachigi);
		CheckBox firstMidEnd = new CheckBox(context);
		firstMidEnd.setText(R.string.first_mid_end);
		settings.addView(moajugi);
		settings.addView(fullMoachigi);
		settings.addView(firstMidEnd);
		moajugi.setChecked(getMoajugi());
		fullMoachigi.setChecked(getFullMoachigi());
		firstMidEnd.setChecked(getFirstMidEnd());
		moajugi.setOnCheckedChangeListener((v, checked) -> setMoajugi(checked));
		fullMoachigi.setOnCheckedChangeListener((v, checked) -> setFullMoachigi(checked));
		firstMidEnd.setOnCheckedChangeListener((v, checked) -> setFirstMidEnd(checked));
		return settings;
	}
}
