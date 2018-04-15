package io.github.lee0701.heonot.inputmethod.modules.generator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import io.github.lee0701.heonot.R;
import io.github.lee0701.heonot.inputmethod.event.*;

import static io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.*;

public class UnicodeCharacterGenerator extends CharacterGenerator {

	private Stack<State> states = new Stack<>();

	private List<Combination> combinationTable = new ArrayList<>();

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

	private void fireComposeCharEvent(State state) {
		EventBus.getDefault().post(new ComposeCharEvent(state.composing, state.lastInput, state.syllable.cho, state.syllable.jung, state.syllable.jong));
	}

	@Override
	public void input(long code) {
		State state = this.processInput(code);
		fireComposeCharEvent(state);
		states.push(state);
	}

	private State processInput(long code) {
		if(states.empty()) states.push(new State());
		State state = new State(states.peek());

		int codeType = (int) ((code & 0xff000000) >> 0x18);
		int codePoint = (int) (code & 0x00ffffff);
		if((codePoint & 0x00ff0000) != 0) {
			commitComposingChar();
			EventBus.getDefault().post(new CommitStringEvent(new String(Character.toChars(codePoint)), 1));
			state = states.pop();
			state.lastInput = State.INPUT_NON_HANGUL;
			state.last = codePoint;
			return state;
		}

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
				Character combination = getCombinationResult(pair);
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
				Character combination = getCombinationResult(pair);
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
				Character combination = getCombinationResult(pair);
				if(combination != null) {
					state.beforeJong = state.syllable.jong;
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

		case CHO2: {
			// 초성과 중성이 존재할 경우
			if(state.syllable.containsCho() && state.syllable.containsJung()) {
				// 한글 자모 종성으로 변환.
				char compatCode = charCode;
				charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.JONG3);
				state.lastInput = 0;
				// 해당하는 종성이 없을 경우
				if(charCode == 0) {
					charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.CHO3);
					commitComposingChar();
					startNewSyllable(new UnicodeHangulSyllable(charCode, (char) 0, (char) 0));
					state = states.pop();
					state.lastInput |= State.INPUT_NO_MATCHING_JONG | State.INPUT_NEW_SYLLABLE_STARTED;
					state.lastInput |= State.INPUT_CHO2;
					break;
				} else if(state.syllable.containsJong()) {
					JamoPair pair = new JamoPair(state.syllable.jong, charCode);
					Character combination = getCombinationResult(pair);
					if(combination != null) {
						state.beforeJong = state.syllable.jong;
						state.syllable.jong = combination;
						state.lastInput |= State.INPUT_COMBINED;
					} else {
						commitComposingChar();
						charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.CHO3);
						startNewSyllable(new UnicodeHangulSyllable(charCode, (char) 0, (char) 0));
						state = states.pop();
						state.lastInput |= State.INPUT_COMBINATION_FAILED | State.INPUT_NEW_SYLLABLE_STARTED;
					}
				} else {
					state.syllable.jong = charCode;
				}
				state.lastInput |= State.INPUT_JONG2;
			} else {
				// 한글 자모 초성으로 변환
				charCode = convertCompatibleJamo(charCode, UnicodeJamoHandler.JamoType.CHO3);
				if(!moajugi && state.syllable.containsJung()) {
					commitComposingChar();
					state = new State(states.peek());
				}
				state.lastInput = 0;
				// 초성이 존재할 경우
				if(state.syllable.containsCho()) {
					// 낱자 조합 시도
					JamoPair pair = new JamoPair(state.syllable.cho, charCode);
					Character combination = getCombinationResult(pair);
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
				state.lastInput |= State.INPUT_CHO2;
			}
			break;
		}

		case JUNG2: {
			charCode = convertCompatibleJamo(charCode, UnicodeJamoHandler.JamoType.JUNG3);
			// 종성이 존재할 경우 (도깨비불 발생)
			if(state.syllable.containsJong()) {
				state.lastInput = 0;
				if(state.beforeJong != 0) {
					state.syllable.jong = state.beforeJong;
				} else {
					state.syllable.jong = 0;
				}
				char cho = convertToCho((char) state.last);
				state.composing = state.syllable.toString(getFirstMidEnd());
				fireComposeCharEvent(state);
				commitComposingChar();
				startNewSyllable(new UnicodeHangulSyllable(cho, (char) 0, (char) 0));
				state = states.pop();
				state.composing = state.syllable.toString(getFirstMidEnd());
				states.push(new State(state));
				state.syllable.jung = charCode;
			} else {
				state.lastInput = 0;
				// 낱자 결합 시도
				if(state.syllable.containsJung()) {
					JamoPair pair = new JamoPair(state.syllable.jung, charCode);
					Character combination = getCombinationResult(pair);
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
				state.lastInput |= State.INPUT_JUNG2;
			}
			break;
		}

		default:
			commitComposingChar();
			EventBus.getDefault().post(new CommitCharEvent(charCode, 1));
			state = states.pop();
			state.lastInput = State.INPUT_NON_HANGUL;
		}

		state.last = charCode;

		state.composing = state.syllable.toString(getFirstMidEnd());

		return state;
	}

	@Override
	public void backspace() {
		try {
			states.pop();
			State state = states.peek();
			fireComposeCharEvent(state);
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

	public Character getCombinationResult(JamoPair pair) {
		try {
			return this.getCombination(pair).getResult();
		} catch(NullPointerException e) {
			return null;
		}
	}

	public Combination getCombination(JamoPair pair) {
		return this.getCombination(false, pair);
	}

	public Combination getCombination(boolean sort, JamoPair pair) {
		sortCombinationTable();
		int key = Collections.binarySearch(combinationTable, pair);
		if(key < 0) return null;
		return combinationTable.get(key);
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
		public static final int INPUT_NO_MATCHING_JONG = 0x0008;

		UnicodeHangulSyllable syllable;
		int last;
		char beforeJong;
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
			syllable = previousState.syllable.clone();
			last = previousState.last;
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
				if(value instanceof List) {
					this.combinationTable = (List<Combination>) value;
					sortCombinationTable();
				} else if(value instanceof JSONObject) {
					this.combinationTable = loadCombinationTable((JSONObject) value);
					sortCombinationTable();
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

	private void sortCombinationTable() {
		Collections.sort(combinationTable, (o1, o2) -> o1.compareTo(o2.getSource()));
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
		for(Combination comb : this.combinationTable) {
			Character result = comb.getResult();
			JSONObject entry = new JSONObject();
			entry.put("a", comb.getSource().a);
			entry.put("b", comb.getSource().b);
			entry.put("result", Integer.toString(result));
			combination.put(entry);
		}
		object.put("combination", combination);
		return object;
	}

	public List<Combination> getCombinationTable() {
		return combinationTable;
	}

	public void setCombinationTable(List<Combination> combinationTable) {
		this.combinationTable = combinationTable;
	}

	public static List<Combination> loadCombinationTable(JSONObject jsonObject) throws JSONException {
		List<Combination> combinationTable = new ArrayList<>();

		JSONArray combination = jsonObject.getJSONArray("combination");
		if(combination != null) {
			for(int i = 0 ; i < combination.length() ; i++) {
				JSONObject o = combination.getJSONObject(i);
				int a = o.getInt("a");
				int b = o.getInt("b");
				String result = o.getString("result");
				JamoPair pair = new JamoPair((char) a, (char) b);
				combinationTable.add(new Combination(pair, (char) Integer.parseInt(result)));
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
	public UnicodeCharacterGenerator clone() {
		UnicodeCharacterGenerator cloned = new UnicodeCharacterGenerator();
		List<Combination> combinationTable = new ArrayList<>();
		if(combinationTable != null) {
			for(Combination combination : this.combinationTable) {
				combinationTable.add(combination.clone());
			}
			cloned.setCombinationTable(combinationTable);
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

		settings.addView(super.createSettingsView(context));

		CheckBox moajugi = new CheckBox(context);
		moajugi.setText(R.string.moajugi);
		moajugi.setChecked(getMoajugi());
		moajugi.setOnCheckedChangeListener((v, checked) -> setMoajugi(checked));
		settings.addView(moajugi);

		CheckBox fullMoachigi = new CheckBox(context);
		fullMoachigi.setText(R.string.full_moachigi);
		fullMoachigi.setChecked(getFullMoachigi());
		fullMoachigi.setOnCheckedChangeListener((v, checked) -> setFullMoachigi(checked));
		settings.addView(fullMoachigi);

		CheckBox firstMidEnd = new CheckBox(context);
		firstMidEnd.setText(R.string.first_mid_end);
		firstMidEnd.setChecked(getFirstMidEnd());
		firstMidEnd.setOnCheckedChangeListener((v, checked) -> setFirstMidEnd(checked));
		settings.addView(firstMidEnd);

		RecyclerView recyclerView = new RecyclerView(context);
		CombinationTableAdapter adapter = new CombinationTableAdapter(context);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setNestedScrollingEnabled(false);

		ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0,
				ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
				int index = Collections.binarySearch(combinationTable, ((CombinationTableViewHolder)viewHolder).jamoPair);
				if(index < 0) return;
				combinationTable.remove(index);
				sortCombinationTable();
				adapter.notifyDataSetChanged();
			}
		};

		LinearLayout title = new LinearLayout(context);

		TextView titleText = new TextView(context);
		titleText.setText(R.string.combination_table);
		titleText.setTextSize(TypedValue.COMPLEX_UNIT_PT, 11);
		title.addView(titleText);

		Button addButton = new Button(context);
		addButton.setText(R.string.button_add);
		addButton.setOnClickListener((v) -> {
			ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.combination_table_edit, null);
			EditText left = (EditText) view.findViewById(R.id.combination_left);
			EditText right = (EditText) view.findViewById(R.id.combination_right);
			EditText result = (EditText) view.findViewById(R.id.combination_result);
			new AlertDialog.Builder(context)
					.setView(view)
					.setPositiveButton(R.string.button_ok, (dialog, which) -> {
						try {
							JamoPair pair = new JamoPair((char) parseCharCode(left.getText().toString()), (char) parseCharCode(right.getText().toString()));
							int index;
							if((index = Collections.binarySearch(combinationTable, pair)) >= 0) {
								Toast.makeText(context, R.string.msg_same_value_overwritten, Toast.LENGTH_SHORT).show();
								combinationTable.remove(index);
							}
							char resultChar = (char) parseCharCode(result.getText().toString());
							combinationTable.add(new Combination(pair, resultChar));
							sortCombinationTable();
							adapter.notifyDataSetChanged();
						} catch(NumberFormatException e) {
							Toast.makeText(context, R.string.msg_illegal_number_format, Toast.LENGTH_SHORT).show();
						}
					})
					.setNegativeButton(R.string.button_cancel, (dialog, which) -> {})
			.create().show();
		});
		title.addView(addButton);

		settings.addView(title);

		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(recyclerView);
		settings.addView(recyclerView);

		return settings;
	}

	class CombinationTableViewHolder extends RecyclerView.ViewHolder{
		TextView combinationLeft, combinationRight, combinationResult;
		JamoPair jamoPair;

		CombinationTableViewHolder(View itemView) {
			super(itemView);
			combinationLeft = (TextView)itemView.findViewById(R.id.combination_left_text);
			combinationRight = (TextView)itemView.findViewById(R.id.combination_right_text);
			combinationResult = (TextView)itemView.findViewById(R.id.combination_result_text);
		}

		@SuppressLint("SetTextI18n")
		void onBind(UnicodeJamoHandler.JamoPair jamoPair, char character){
			this.jamoPair = jamoPair;
			combinationLeft.setText(jamoPair.a + " (" + UnicodeJamoHandler.getType(jamoPair.a).name() +")");
			combinationRight.setText(jamoPair.b + " (" + UnicodeJamoHandler.getType(jamoPair.b).name() +")");
			combinationResult.setText(character + " (" + UnicodeJamoHandler.getType(character).name() +")");
		}
	}
	class CombinationTableAdapter extends RecyclerView.Adapter<CombinationTableViewHolder>{

		private Context context;
		CombinationTableAdapter(Context context){
			this.context = context;
		}
		@Override
		public CombinationTableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View combinationTableView = LayoutInflater
					.from(context)
					.inflate(R.layout.combination_table_view_holder, parent, false);
			return new CombinationTableViewHolder(combinationTableView);
		}

		@Override
		public void onBindViewHolder(CombinationTableViewHolder holder, int position) {
			Combination combination = combinationTable.get(position);
			holder.onBind(combination.getSource(), combination.getResult());
		}

		@Override
		public int getItemCount() {
			return combinationTable.size();
		}
	}

	private static class Combination implements Cloneable, Comparable<JamoPair> {
		JamoPair source;
		char result;

		public Combination(JamoPair source, char result) {
			this.source = source;
			this.result = result;
		}

		@Override
		public int compareTo(@NonNull JamoPair o) {
			return source.compareTo(o);
		}

		@Override
		public Combination clone() {
			return new Combination(source.clone(), result);
		}

		public JamoPair getSource() {
			return source;
		}

		public char getResult() {
			return result;
		}
	}
}
