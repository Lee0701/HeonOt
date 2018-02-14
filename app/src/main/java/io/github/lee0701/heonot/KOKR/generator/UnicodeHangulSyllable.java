package io.github.lee0701.heonot.KOKR.generator;

public class UnicodeHangulSyllable implements Cloneable {

	public static char[] CHO_TABLE = {
			'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
			'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
			'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ',
	};
	public static char[] JUNG_TABLE = {
			'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ',
			'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
			'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ',
			'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ',
			'ㅣ',
	};
	public static char[] JONG_TABLE = {
			' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ',
			'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
			'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ',
			'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
			'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ',
			'ㅌ', 'ㅍ', 'ㅎ'
	};

	public char cho, jung, jong;

	public UnicodeHangulSyllable(char cho, char jung, char jong) {
		this.cho = cho;
		this.jung = jung;
		this.jong = jong;
	}

	public UnicodeHangulSyllable() {
	}

	public boolean containsCho() {
		return cho != 0;
	}

	public boolean containsJung() {
		return jung != 0;
	}

	public boolean containsJong() {
		return jong != 0;
	}

	public boolean containsTraditionalJamo() {
		return cho >= 0xa960 || jung >= 0xd7b0 || jong >= 0xd7cb;
	}

	public boolean isIncomplete() {
		return (containsCho() != containsJung()) && containsJong();
	}

	public boolean requiresFirstMidEndComposition() {
		return containsTraditionalJamo() || isIncomplete();
	}

	public String toString(boolean allowFirstMidEnd) {
		if(this.requiresFirstMidEndComposition()) {
			char cho = containsCho() ? this.cho : 0x115f;
			char jung = containsJung() ? this.jung : 0x1160;
			return new String(new char[] {cho, jung, jong});
		} else {
			int cho = this.cho - 0x1100;
			int jung = this.jung - 0x1161;
			int jong = containsJong() ? this.jong - 0x11a8 + 1 : 0;
			if(containsCho() && containsJung()) {
				return String.valueOf(compose(cho, jung, jong));
			} else if(containsCho()) {
				return String.valueOf(CHO_TABLE[cho]);
			} else if(containsJung()) {
				return String.valueOf(JUNG_TABLE[jung]);
			} else if(containsJong()) {
				return String.valueOf(JONG_TABLE[jong]);
			}
		}
		return "";
	}

	public String toString() {
		return this.toString(true);
	}

	public Object clone() {
		return new UnicodeHangulSyllable(cho, jung, jong);
	}

	public static char compose(int cho, int jung, int jong) {
		return (char) ((cho * 588) + (jung * 28) + jong + 0xac00);
	}

}