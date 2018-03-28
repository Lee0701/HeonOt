package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public abstract class TreeNode implements Cloneable {

	public static final int TYPE_CONSTANT = 1;
	public static final int TYPE_VARIABLE = 3;
	public static final int TYPE_UNARY = 8;
	public static final int TYPE_BINARY = 9;
	public static final int TYPE_TERNARY = 10;
	public static final int TYPE_LIST = 11;

	private Operator operator;
	public TreeNode(Operator operator) {
		this.operator = operator;
	}

	public abstract TreeNode clone();

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
}
