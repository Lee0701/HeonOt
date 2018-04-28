package io.github.lee0701.heonot.inputmethod.modules.generator

import io.github.lee0701.heonot.inputmethod.event.*
import io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.JamoPair
import io.github.lee0701.heonot.inputmethod.modules.generator.UnicodeJamoHandler.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class UnicodeCharacterGenerator : CharacterGenerator() {

	protected val states = Stack<State>()

	protected var combinationTable: MutableList<Combination> = ArrayList()

	protected var moajugi: Boolean = false
	protected var fullMoachigi: Boolean = false
	protected var firstMidEnd: Boolean = false

	override fun init() {
		//Push initial placeholder state into the stack.
		states.push(State())
	}

	override fun pause() {

	}

	private fun fireComposeCharEvent(state: State) {
		EventBus.getDefault().post(ComposeCharEvent(state.composing, state.lastInput, state.syllable.cho.toInt(), state.syllable.jung.toInt(), state.syllable.jong.toInt()))
	}

	override fun input(code: Long) {
		val state = this.processInput(code)
		fireComposeCharEvent(state)
		EventBus.getDefault().post(AutomataStateChangeEvent(state.status))
		states.push(state)
	}

	private fun processInput(code: Long): State {
		if (states.empty()) states.push(State())
		var state = State(states.peek())

		val codeType = (code and -0x1000000 shr 0x18).toInt()
		val advanceStatus = (codeType and CODE_NO_CHANGE_STATUS) == 0
		val codePoint = (code and 0x00ffffff).toInt()
		if (codePoint and 0x00ff0000 != 0) {
			commitComposingChar()
			EventBus.getDefault().post(CommitStringEvent(String(Character.toChars(codePoint)), 1))
			state = states.pop()
			state.lastInput = State.INPUT_NON_HANGUL
			state.last = codePoint
			return state
		}

		var charCode = (code and 0xffff).toChar()
		when (UnicodeJamoHandler.getType(charCode)) {
			UnicodeJamoHandler.JamoType.CHO3 -> {
				if (!fullMoachigi) {
					if (!moajugi) {
						if (state.lastInput and State.MASK_HANGUL_TYPE != State.INPUT_CHO) {
							commitComposingChar()
							state = State(states.peek())
						}
					} else {
						if ((state.syllable.containsJung() || state.syllable.containsJong()) && state.syllable.containsCho()) {
							commitComposingChar()
							state = State(states.peek())
						}
					}
				}
				state.lastInput = 0
				if (state.syllable.containsCho()) {
					val pair = JamoPair(state.syllable.cho, charCode)
					val combination = getCombinationResult(pair)
					if (combination != null) {
						state.syllable.cho = combination
						state.lastInput = state.lastInput or State.INPUT_COMBINED
					} else {
						commitComposingChar()
						startNewSyllable(UnicodeHangulSyllable(charCode, 0.toChar(), 0.toChar()))
						state = states.pop()
						state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
					}
				} else {
					state.syllable.cho = charCode
				}
				state.lastInput = state.lastInput or State.INPUT_CHO3
				if(advanceStatus) state.status = State.STATUS_CHO
			}

			UnicodeJamoHandler.JamoType.JUNG3 -> {
				if (!moajugi && !fullMoachigi) {
					when (state.lastInput and State.MASK_HANGUL_TYPE) {
						State.INPUT_CHO, State.INPUT_JUNG -> {
						}

						else -> {
							commitComposingChar()
							state = State(states.peek())
						}
					}
				}
				state.lastInput = 0
				if (state.syllable.containsJung()) {
					val pair = JamoPair(state.syllable.jung, charCode)
					val combination = getCombinationResult(pair)
					if (combination != null) {
						state.syllable.jung = combination
						state.lastInput = state.lastInput or State.INPUT_COMBINED
					} else {
						commitComposingChar()
						startNewSyllable(UnicodeHangulSyllable(0.toChar(), charCode, 0.toChar()))
						state = states.pop()
						state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
					}
				} else {
					state.syllable.jung = charCode
				}
				state.lastInput = state.lastInput or State.INPUT_JUNG3
				if(advanceStatus) state.status = State.STATUS_JUNG
			}

			UnicodeJamoHandler.JamoType.JONG3 -> {
				if (!moajugi && !fullMoachigi) {
					when (state.lastInput and State.MASK_HANGUL_TYPE) {
						State.INPUT_JUNG, State.INPUT_JONG -> {
						}

						else -> {
							commitComposingChar()
							state = State(states.peek())
						}
					}
				}
				state.lastInput = 0
				if (state.syllable.containsJong()) {
					val pair = JamoPair(state.syllable.jong, charCode)
					val combination = getCombinationResult(pair)
					if (combination != null) {
						state.beforeJong = state.syllable.jong
						state.syllable.jong = combination
						state.lastInput = state.lastInput or State.INPUT_COMBINED
					} else {
						commitComposingChar()
						startNewSyllable(UnicodeHangulSyllable(0.toChar(), 0.toChar(), charCode))
						state = states.pop()
						state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
					}
				} else {
					state.syllable.jong = charCode
				}
				state.lastInput = state.lastInput or State.INPUT_JONG3
				if(advanceStatus) state.status = State.STATUS_JONG
			}

			UnicodeJamoHandler.JamoType.CHO2 -> run {
				// 초성과 중성이 존재할 경우
				if (state.syllable.containsCho() && state.syllable.containsJung()) {
					// 한글 자모 종성으로 변환.
					val compatCode = charCode
					charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.JONG3)
					state.lastInput = 0
					// 해당하는 종성이 없을 경우
					if (charCode.toInt() == 0) {
						charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.CHO3)
						commitComposingChar()
						startNewSyllable(UnicodeHangulSyllable(charCode, 0.toChar(), 0.toChar()))
						state = states.pop()
						state.lastInput = state.lastInput or (State.INPUT_NO_MATCHING_JONG or State.INPUT_NEW_SYLLABLE_STARTED)
						state.lastInput = state.lastInput or State.INPUT_CHO2
						if(advanceStatus) state.status = State.STATUS_CHO
						return@run
					} else if (state.syllable.containsJong()) {
						val pair = JamoPair(state.syllable.jong, charCode)
						val combination = getCombinationResult(pair)
						if (combination != null) {
							state.beforeJong = state.syllable.jong
							state.syllable.jong = combination
							state.lastInput = state.lastInput or State.INPUT_COMBINED
						} else {
							commitComposingChar()
							charCode = convertCompatibleJamo(compatCode, UnicodeJamoHandler.JamoType.CHO3)
							startNewSyllable(UnicodeHangulSyllable(charCode, 0.toChar(), 0.toChar()))
							state = states.pop()
							state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
						}
					} else {
						state.syllable.jong = charCode
					}
					state.lastInput = state.lastInput or State.INPUT_JONG2
					if(advanceStatus) state.status = State.STATUS_JONG
				} else {
					// 한글 자모 초성으로 변환
					charCode = convertCompatibleJamo(charCode, UnicodeJamoHandler.JamoType.CHO3)
					if (!moajugi && state.syllable.containsJung()) {
						commitComposingChar()
						state = State(states.peek())
					}
					state.lastInput = 0
					// 초성이 존재할 경우
					if (state.syllable.containsCho()) {
						// 낱자 조합 시도
						val pair = JamoPair(state.syllable.cho, charCode)
						val combination = getCombinationResult(pair)
						if (combination != null) {
							state.syllable.cho = combination
							state.lastInput = state.lastInput or State.INPUT_COMBINED
						} else {
							commitComposingChar()
							startNewSyllable(UnicodeHangulSyllable(charCode, 0.toChar(), 0.toChar()))
							state = states.pop()
							state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
						}
					} else {
						state.syllable.cho = charCode
					}
					state.lastInput = state.lastInput or State.INPUT_CHO2
					if(advanceStatus) state.status = State.STATUS_CHO
				}
			}

			UnicodeJamoHandler.JamoType.JUNG2 -> {
				charCode = convertCompatibleJamo(charCode, UnicodeJamoHandler.JamoType.JUNG3)
				// 종성이 존재할 경우 (도깨비불 발생)
				if (state.syllable.containsJong()) {
					state.lastInput = 0
					if(advanceStatus) state.status = State.STATUS_INITIAL
					if (state.beforeJong.toInt() != 0) {
						state.syllable.jong = state.beforeJong
					} else {
						state.syllable.jong = 0.toChar()
					}
					val cho = convertToCho(state.last.toChar())
					state.composing = state.syllable.toString(firstMidEnd)
					fireComposeCharEvent(state)
					commitComposingChar()
					startNewSyllable(UnicodeHangulSyllable(cho, 0.toChar(), 0.toChar()))
					state = states.pop()
					state.composing = state.syllable.toString(firstMidEnd)
					states.push(State(state))
					state.syllable.jung = charCode
				} else {
					state.lastInput = 0
					// 낱자 결합 시도
					if (state.syllable.containsJung()) {
						val pair = JamoPair(state.syllable.jung, charCode)
						val combination = getCombinationResult(pair)
						if (combination != null) {
							state.syllable.jung = combination
							state.lastInput = state.lastInput or State.INPUT_COMBINED
						} else {
							commitComposingChar()
							startNewSyllable(UnicodeHangulSyllable(0.toChar(), charCode, 0.toChar()))
							state = states.pop()
							state.lastInput = state.lastInput or (State.INPUT_COMBINATION_FAILED or State.INPUT_NEW_SYLLABLE_STARTED)
						}
					} else {
						state.syllable.jung = charCode
					}
					state.lastInput = state.lastInput or State.INPUT_JUNG2
					if(advanceStatus) state.status = State.STATUS_JUNG
				}
			}

			else -> {
				commitComposingChar()
				EventBus.getDefault().post(CommitCharEvent(charCode, 1))
				state = states.pop()
				state.lastInput = State.INPUT_NON_HANGUL
			}
		}

		state.last = charCode.toInt()

		state.composing = state.syllable.toString(firstMidEnd)

		return state
	}

	override fun backspace() {
		try {
			states.pop()
			val state = states.peek()
			fireComposeCharEvent(state)
			EventBus.getDefault().post(AutomataStateChangeEvent(state.status))
		} catch (e: EmptyStackException) {
			EventBus.getDefault().post(FinishComposingEvent())
			EventBus.getDefault().post(DeleteCharEvent(1, 0))
		}

	}

	fun commitComposingChar() {
		EventBus.getDefault().post(FinishComposingEvent())
		states.clear()
		states.push(State())
		EventBus.getDefault().post(AutomataStateChangeEvent(0))
	}

	fun startNewSyllable(syllable: UnicodeHangulSyllable) {
		states.push(State(syllable))
	}

	fun getCombinationResult(pair: JamoPair): Char? {
		try {
			return this.getCombination(pair)!!.result
		} catch (e: NullPointerException) {
			return null
		}

	}

	fun getCombination(pair: JamoPair): Combination? {
		return this.getCombination(false, pair)
	}

	fun getCombination(sort: Boolean, pair: JamoPair): Combination? {
		sortCombinationTable()
		val key = Collections.binarySearch<JamoPair>(combinationTable, pair)
		return if (key < 0) null else combinationTable[key]
	}

	companion object {
		val CODE_NO_CHANGE_STATUS = 1
	}

	class State {

		internal var syllable: UnicodeHangulSyllable
		internal var last: Int = 0
		internal var beforeJong: Char = ' '
		internal var composing: String
		internal var lastInput: Int = 0
		internal var status = 0

		@JvmOverloads internal constructor(syllable: UnicodeHangulSyllable = UnicodeHangulSyllable()) {
			this.syllable = syllable
			last = 0
			beforeJong = 0.toChar()
			composing = ""
			lastInput = 0
		}

		internal constructor(previousState: State) {
			syllable = previousState.syllable.clone()
			last = previousState.last
			beforeJong = previousState.beforeJong
			composing = previousState.composing
			lastInput = previousState.lastInput
			status = previousState.status
		}

		companion object {

			val STATUS_INITIAL = 0
			val STATUS_CHO = 1
			val STATUS_JUNG = 2
			val STATUS_JONG = 3

			val MASK_HANGUL_BEOL = 0xf000
			val MASK_HANGUL_TYPE = 0x0f00

			val INPUT_NON_HANGUL = 0x0000

			val INPUT_DUBEOL = 0x2000
			val INPUT_SEBEOL = 0x3000

			val INPUT_CHO = 0x0100
			val INPUT_JUNG = 0x0200
			val INPUT_JONG = 0x0300

			val INPUT_CHO3 = 0x3100
			val INPUT_JUNG3 = 0x3200
			val INPUT_JONG3 = 0x3300

			val INPUT_CHO2 = 0x2100
			val INPUT_JUNG2 = 0x2200
			val INPUT_JONG2 = 0x2300

			val INPUT_COMBINED = 0x0001
			val INPUT_COMBINATION_FAILED = 0x0002
			val INPUT_NEW_SYLLABLE_STARTED = 0x0004
			val INPUT_NO_MATCHING_JONG = 0x0008
		}

	}

	@Subscribe
	fun onInputChar(event: InputCharEvent) {
		val o = event.character
		if (o is Long)
			this.input(o)
		else if (o is Int) this.input(o.toLong())
	}

	@Subscribe
	fun onCommitComposingChar(event: CommitComposingCharEvent) {
		commitComposingChar()
	}

	@Subscribe(priority = 1)
	fun onBackspace(event: BackspaceEvent) {
		this.backspace()
		EventBus.getDefault().cancelEventDelivery(event)
	}

	override fun setProperty(key: String, value: Any) {
		when (key) {
			"combination-table" -> try {
				if (value is List<*>) {
					this.combinationTable = (value as List<Combination>).toMutableList()
					sortCombinationTable()
				} else if (value is JSONObject) {
					this.combinationTable = loadCombinationTable(value)
					sortCombinationTable()
				}
			} catch (ex: Exception) {
				ex.printStackTrace()
			}

			"moajugi" -> try {
				this.moajugi = value as Boolean
			} catch (ex: ClassCastException) {
				ex.printStackTrace()
			} catch (ex: NullPointerException) {
				ex.printStackTrace()
			}

			"first-mid-end" -> try {
				this.firstMidEnd = value as Boolean
			} catch (ex: ClassCastException) {
				ex.printStackTrace()
			} catch (ex: NullPointerException) {
				ex.printStackTrace()
			}

			"full-moachigi" -> try {
				this.fullMoachigi = value as Boolean
			} catch (ex: ClassCastException) {
				ex.printStackTrace()
			} catch (ex: NullPointerException) {
				ex.printStackTrace()
			}

		}
	}

	protected fun sortCombinationTable() {
		Collections.sort(combinationTable) { o1, o2 -> o1.compareTo(o2.source) }
	}

	@Throws(JSONException::class)
	override fun toJSONObject(): JSONObject {
		val `object` = super.toJSONObject()
		val properties = JSONObject()

		properties.put("combination-table", this.storeCombinationTable())

		properties.put("moajugi", this.moajugi)
		properties.put("first-mid-end", this.firstMidEnd)
		properties.put("full-moachigi", this.fullMoachigi)

		`object`.put("properties", properties)

		return `object`
	}

	@Throws(JSONException::class)
	fun storeCombinationTable(): JSONObject {
		val `object` = JSONObject()
		val combination = JSONArray()
		for (comb in this.combinationTable) {
			val result = comb.result
			val entry = JSONObject()
			entry.put("a", comb.source.a.toInt())
			entry.put("b", comb.source.b.toInt())
			entry.put("result", Integer.toString(result.toInt()))
			combination.put(entry)
		}
		`object`.put("combination", combination)
		return `object`
	}

	@Throws(JSONException::class)
	fun loadCombinationTable(jsonObject: JSONObject): MutableList<Combination> {
		val combinationTable = ArrayList<Combination>()

		val combination = jsonObject.getJSONArray("combination")
		if (combination != null) {
			for (i in 0 until combination.length()) {
				val o = combination.getJSONObject(i)
				val a = o.getInt("a")
				val b = o.getInt("b")
				val result = o.getString("result")
				val pair = JamoPair(a.toChar(), b.toChar())
				combinationTable.add(Combination(pair, Integer.parseInt(result).toChar()))
			}
		}

		return combinationTable
	}

	override fun clone(): UnicodeCharacterGenerator {
		val cloned = UnicodeCharacterGenerator()
		val combinationTable = ArrayList<Combination>()
		if (combinationTable != null) {
			for (combination in this.combinationTable) {
				combinationTable.add(combination.clone())
			}
			cloned.combinationTable = combinationTable
		}
		cloned.moajugi = moajugi
		cloned.fullMoachigi = fullMoachigi
		cloned.firstMidEnd = firstMidEnd
		cloned.name = this.name
		return cloned
	}

	class Combination(source: JamoPair, result: Char) : Cloneable, Comparable<JamoPair> {
		var source: JamoPair
			internal set
		var result: Char = ' '
			internal set

		init {
			this.source = source
			this.result = result
		}

		override fun compareTo(o: JamoPair): Int {
			return source.compareTo(o)
		}

		public override fun clone(): Combination {
			return Combination(source.clone(), result)
		}
	}
}