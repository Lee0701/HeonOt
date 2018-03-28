package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public enum Operator {
	NONE(0, ""),

	PLUS(1, "+"),
	MINUS(2, "-"),
	NOT(3, "!"),
	INVERT(4, "~"),

	INCREMENT_LEFT(5, "++"),
	INCREMENT_RIGHT(6, "++"),
	DECREMENT_LEFT(7, "--"),
	DECREMENT_RIGHT(8, "--"),

	MULTIPLICATION(9, "*"),
	DIVISION(10, "/"),
	MOD(11, "%"),
	ADDITION(12, "+"),
	SUBTRACTION(13, "-"),

	SHIFT_LEFT(14, "<<"),
	SHIFT_RIGHT(15, ">>"),

	COMPARE_GREATER(16, ">"),
	COMPARE_SMALLER(17, "<"),
	COMPARE_GREATER_OR_EQUAL(18, ">="),
	COMPARE_SMALLER_OR_EQUAL(19, "<="),

	EQUALS(20, "=="),
	NOT_EQUALS(21, "!="),

	BITWISE_AND(22, "&"),
	BITWISE_OR(23, "|"),
	BITWISE_XOR(24, "^"),

	LOGICAL_AND(25, "&&"),
	LOGICAL_OR(26, "||"),

	CONDITION(27, "?"),

	ASSIGNMENT(32, "="),
	ASSIGNMENT_ADDITION(33, "+="),
	ASSIGNMENT_SUBTRACTION(34, "-="),
	ASSIGNMENT_MULTIPLICATION(35, "*="),
	ASSIGNMENT_DIVISION(36, "-="),
	ASSIGNMENT_MOD(37, "%="),

	ASSIGNMENT_SHIFT_LEFT(38, "<<="),
	ASSIGNMENT_SHIFT_RIGHT(39, ">>="),
	ASSIGNMENT_AND(40, "&="),
	ASSIGNMENT_OR(41, "|="),
	ASSIGNMENT_XOR(42, "^="),

	COMMA(43, ",");

	int code;
	String rep;

	Operator(int code, String rep) {
		this.code = code;
		rep = rep;
	}

	public int getCode() {
		return code;
	}

	public String getRep() {
		return rep;
	}
}
