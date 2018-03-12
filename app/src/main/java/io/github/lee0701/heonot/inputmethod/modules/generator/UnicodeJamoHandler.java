package io.github.lee0701.heonot.inputmethod.modules.generator;

import android.support.annotation.NonNull;

public final class UnicodeJamoHandler {

	private static char[] CHO_CONVERT = {
			0x1100, 0x1101, 0x0000, 0x1102, 0x0000, 0x115d, 0x1103,		// 0x3130 (0x115d: traditional)
			0x1104, 0x1105, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3138
			0x111a, 0x1106, 0x1107, 0x1108, 0x0000, 0x1109, 0x110a, 0x110b,		// 0x3140 (0x111a: traditional)
			0x110c, 0x110d, 0x110e, 0x110f, 0x1110, 0x1111, 0x1112, 0x0000,		// 0x3148
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3150
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3158
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x1114, 0x1115, 0x0000,		// 0x3160
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x111c, 0x0000,		// 0x3168
			0x0000, 0x111d, 0x111e, 0x1120, 0x1122, 0x1123, 0x1127, 0x1129,		// 0x3170
			0x112b, 0x112c, 0x112d, 0x112e, 0x112f, 0x1132, 0x1136, 0x1140,		// 0x3178
			0x1147, 0x114c, 0x0000, 0x0000, 0x1157, 0x1158, 0x1159,				// 0x3180
	};

	private static char[] JONG_CONVERT = {
			0x11a8, 0x11a9, 0x11aa, 0x11ab, 0x11ac, 0x11ad, 0x11ae,		// 0x3130
			0x0000, 0x11af, 0x11b0, 0x11b1, 0x11b2, 0x11b3, 0x11b4, 0x11b5,		// 0x3138
			0x11b6, 0x11b7, 0x11b8, 0x0000, 0x11b9, 0x11ba, 0x11bb, 0x11bc,		// 0x3140
			0x11bd, 0x0000, 0x11be, 0x11bf, 0x11c0, 0x11c1, 0x11c2, 0x0000,		// 0x3148
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3150
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3158
			0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x11c6, 0x11c7,		// 0x3160
			0x11c8, 0x11cc, 0x11ce, 0x11d3, 0x11d7, 0x11d9, 0x11dc, 0x11dd,		// 0x3168
			0x11df, 0x11e2, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000,		// 0x3170
			0x11e6, 0x0000, 0x11e7, 0x0000, 0x11e8, 0x11ea, 0x0000, 0x11eb,		// 0x3178
			0x0000, 0x11f0, 0x11f1, 0x11f2, 0x11f4, 0x0000, 0x11f9				// 0x3180
	};

	private static char[] TRAD_JUNG_CONVERT = {
			0x1184, 0x1185, 0x1188, 0x1191, 0x1192, 0x1194, 0x119e, 0x11a1		// 0x3187
	};

	private UnicodeJamoHandler() {
		throw new UnsupportedOperationException("You cannot instantiate UnicodeJamoHandler");
	}

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
		return code >= 0x314f && code <= 0x3163 || code >= 0x3187 && code <= 0x318e;
	}

	public static boolean isDubeolTraditionalJung(char code) {
		return code >= 0x3187 && code <= 0x318e;
	}

	public static JamoType getType(char code) {
		if(isSebeolCho(code)) return JamoType.CHO3;
		else if(isSebeolJung(code)) return JamoType.JUNG3;
		else if(isSebeolJong(code)) return JamoType.JONG3;
		else if(isDubeolCho(code)) return JamoType.CHO2;
		else if(isDubeolJung(code)) return JamoType.JUNG2;
		else return JamoType.NON_HANGUL;
	}

	public static char convertCompatibleJamo(char compatibleJamo, JamoType type) {
		switch(getType(compatibleJamo)) {
		case CHO2:
			if(type == JamoType.CHO3) {
				return CHO_CONVERT[compatibleJamo - 0x3131];
			} else if(type == JamoType.JONG3) {
				return JONG_CONVERT[compatibleJamo - 0x3131];
			} else {
				return 0;
			}

		case JUNG2:
			return isDubeolTraditionalJung(compatibleJamo) ? TRAD_JUNG_CONVERT[compatibleJamo - 0x3187] : (char) (compatibleJamo - 0x314f + 0x1161);

		default:
			return 0;
		}
	}

	public static char convertToCho(char jong) {
		if(isSebeolJong(jong)) {
			for(int i = 0 ; i < JONG_CONVERT.length ; i++) {
				if(JONG_CONVERT[i] == jong) return CHO_CONVERT[i];
			}
		}
		return 0;
	}

	public static int parseCharCode(String str) {
		if(str.startsWith("0x")) {
			return Integer.parseInt(str.replaceFirst("0x", ""), 16);
		} else {
			try {
				return Integer.parseInt(str);
			} catch(NumberFormatException e) {
				if(str.length() == 1) {
					return str.charAt(0);
				}
				throw e;
			}
		}
	}

	public enum JamoType {
		NON_HANGUL, CHO3, JUNG3, JONG3, CHO2, JUNG2
	}

	public static class JamoPair implements Cloneable, Comparable<JamoPair> {
		public char a;
		public char b;
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
			return obj != null && obj.getClass().equals(this.getClass()) && obj.hashCode() == this.hashCode();
		}

		@Override
		public int compareTo(@NonNull JamoPair o) {
			return (o.a == a) ? b - o.b : a - o.a;
		}

		@Override
		public JamoPair clone() {
			return new JamoPair(a, b);
		}
	}

}
