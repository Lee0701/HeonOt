package me.blog.hgl1002.openwnn.KOKR;

public class LayoutAlphabet {

	public static final int[][] CONVERT_ENGLISH_DVORAK = {
			{49,0x31,0x21},
			{50,0x32,0x40},
			{51,0x33,0x23},
			{52,0x34,0x24},
			{53,0x35,0x25},
			{54,0x36,0x5e},
			{55,0x37,0x26},
			{56,0x38,0x2a},
			{57,0x39,0x28},
			{48,0x30,0x29},

			{113, 0x27, 0x22},		// q
			{119, 0x2c, 0x3c},		// w
			{101, 0x2e, 0x3e},		// e
			{114, 112, 80},		// r
			{116, 121, 89},		// t
			{121, 102, 70},		// y
			{117, 103, 71},		// u
			{105, 99, 67},		// i
			{111, 114, 82},		// o
			{112, 108, 76},			// p

			{97, 97, 65},		// a
			{115, 111, 79},		// s
			{100, 101, 69},		// d
			{102, 117, 85},		// f
			{103, 105, 73},		// g
			{104, 100, 68},		// h
			{106, 104, 72},		// j
			{107, 116, 84},		// k
			{108, 110, 78},		// l
			{0x3b, 115, 83},	// ;
			{0x27, 0x2d, 0x5f},	// '

			{122, 0x3b, 0x3a},		// z
			{120, 113, 81},		// x
			{99, 106, 74},		// c
			{118, 107, 75},		// v
			{98, 120, 88},			// b
			{110, 98, 66},		// n
			{109, 109, 77},		// m
			{0x2c, 119, 87},	// ,
			{0x2e, 118, 86},	// .
			{0x2f, 122, 90},	// /

			{0x2d, 0x5b, 0x7b},
			{0x3d, 0x5d, 0x7d},
			{0x5b, 0x2f, 0x3f},
			{0x5d, 0x3d, 0x2b},
	};

	public static final int[][] CONVERT_ENGLISH_COLEMAK = {
			{49,0x31,0x21},
			{50,0x32,0x40},
			{51,0x33,0x23},
			{52,0x34,0x24},
			{53,0x35,0x25},
			{54,0x36,0x5e},
			{55,0x37,0x26},
			{56,0x38,0x2a},
			{57,0x39,0x28},
			{48,0x30,0x29},

			{113, 113, 81},		// q
			{119, 119, 87},		// w
			{101, 102, 70},		// e
			{114, 112, 80},		// r
			{116, 103, 71},		// t
			{121, 106, 74},		// y
			{117, 108, 76},		// u
			{105, 117, 85},		// i
			{111, 121, 89},		// o
			{112, 0x3b, 0x3a},			// p

			{97, 97, 65},		// a
			{115, 114, 82},		// s
			{100, 115, 83},		// d
			{102, 116, 84},		// f
			{103, 100, 68},		// g
			{104, 104, 72},		// h
			{106, 110, 78},		// j
			{107, 101, 69},		// k
			{108, 105, 73},		// l
			{0x3b, 111, 79},

			{122, 122, 90},		// z
			{120, 120, 88},		// x
			{99, 99, 67},		// c
			{118, 118, 86},		// v
			{98, 98, 66},			// b
			{110, 107, 75},		// n
			{109, 109, 77},		// m
			{0x2c, 0x2c, 0x3c},
			{0x2e, 0x2e, 0x3e},
			{0x2f, 0x2f, 0x3f},
	};

	public static final int[][] CYCLE_12KEY_ALPHABET = {
			{-201, '.', '@'},
			{-202, 'a', 'b', 'c'},
			{-203, 'd', 'e', 'f'},
			{-204, 'g', 'h', 'i'},
			{-205, 'j', 'k', 'l'},
			{-206, 'm', 'n', 'o'},
			{-207, 'p', 'q', 'r', 's'},
			{-208, 't', 'u', 'v'},
			{-209, 'w', 'x', 'y', 'z'},
			{-210, '-'},
			{-211, '.', ','},
	};

}
