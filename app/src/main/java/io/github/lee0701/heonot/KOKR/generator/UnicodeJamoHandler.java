package io.github.lee0701.heonot.KOKR.generator;

public class UnicodeJamoHandler {

	public static boolean isSebeolCho(char code) {
		return code >= 0x1100 && code <= 0x115f || code >= 0xa960 && code <= 0xa97c;
	}

	public static boolean isSebeolJung(char code) {
		return code >= 0x1160 && code <= 0x11a7 || code >= 0xd7b0 && code <= 0xd7c6;
	}

	public static boolean isSebeolJong(char code) {
		return code >= 0x11a8 && code <= 0x11ff || code >= 0xd7cb && code <= 0xd7fb;
	}

	public static boolean isDubeolCho(char code) {
		return code >= 0x3131 && code <= 0x314e || code >= 0x3165 && code <= 0x3186;
	}

	public static boolean isDubeolJung(char code) {
		return code >= 0x314f && code < 0x3163 || code >= 0x3187 && code <= 0x318e;
	}

	public static JamoType getType(char code) {
		if(isSebeolCho(code)) return JamoType.CHO3;
		else if(isSebeolJung(code)) return JamoType.JUNG3;
		else if(isSebeolJong(code)) return JamoType.JONG3;
		else if(isDubeolCho(code)) return JamoType.CHO2;
		else if(isDubeolJung(code)) return JamoType.JUNG2;
		else return JamoType.NON_HANGUL;
	}

	public enum JamoType {
		NON_HANGUL, CHO3, JUNG3, JONG3, CHO2, JUNG2;
	}

	public static class JamoPair {
		public char a, b;
		public JamoPair(char a, char b) {
			this.a = a;
			this.b = b;
		}
		@Override
		public int hashCode() {
			int result = 1;
			result = 37 * result + (int) a;
			result = 37 * result + (int) b;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			return obj.hashCode() == this.hashCode();
		}
	}

}
