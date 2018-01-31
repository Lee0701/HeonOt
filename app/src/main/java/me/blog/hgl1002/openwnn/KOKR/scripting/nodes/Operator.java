package me.blog.hgl1002.openwnn.KOKR.scripting.nodes;

public class Operator {
	public static final int NONE = 0;

	public static final int PLUS = 1;
	public static final int MINUS = 2;
	public static final int NOT = 3;
	public static final int INVERT = 4;

	public static final int INCREMENT_LEFT = 5;
	public static final int INCREMENT_RIGHT = 6;
	public static final int DECREMENT_LEFT = 7;
	public static final int DECREMENT_RIGHT = 8;

	public static final int MULTIPLICATION = 9;
	public static final int DIVISION = 10;
	public static final int MOD = 11;
	public static final int ADDITION = 12;
	public static final int SUBTRACTION = 13;

	public static final int SHIFT_LEFT = 14;
	public static final int SHIFT_RIGHT = 15;

	public static final int COMPARE_GREATER = 16;
	public static final int COMPARE_SMALLER = 17;
	public static final int COMPARE_GREATER_OR_EQUAL = 18;
	public static final int COMPARE_SMALLER_OR_EQUAL = 19;

	public static final int EQUALS = 20;
	public static final int NOT_EQUALS = 21;

	public static final int BITWISE_AND = 22;
	public static final int BITWISE_OR = 23;
	public static final int BITWISE_XOR = 24;

	public static final int LOGICAL_AND = 25;
	public static final int LOGICAL_OR = 26;

	public static final int CONDITION = 27;

	public static final int ASSIGNMENT = 32;
	public static final int ASSIGNMENT_ADDITION = 33;
	public static final int ASSIGNMENT_SUBTRACTION = 34;
	public static final int ASSIGNMENT_MULTIPLICATION = 35;
	public static final int ASSIGNMENT_DIVISION = 36;
	public static final int ASSIGNMENT_MOD = 37;

	public static final int ASSIGNMENT_SHIFT_LEFT = 38;
	public static final int ASSIGNMENT_SHIFT_RIGHT = 39;
	public static final int ASSIGNMENT_AND = 40;
	public static final int ASSIGNMENT_OR = 41;
	public static final int ASSIGNMENT_XOR = 42;

	public static final int COMMA = 43;
}
