package me.blog.hgl1002.openwnn.KOKR.scripting.nodes;

public abstract class TreeNode {

	public static final int TYPE_CONSTANT = 1;
	public static final int TYPE_VARIABLE = 3;
	public static final int TYPE_UNARY = 8;
	public static final int TYPE_BINARY = 9;
	public static final int TYPE_TERNARY = 10;
	public static final int TYPE_LIST = 11;

	int operator;
	public TreeNode(int operator) {
		this.operator = operator;
	}

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}
}
