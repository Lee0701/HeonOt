package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public class ConstantTreeNode extends TreeNode {

	private String name;
	private long value;

	public ConstantTreeNode(long value) {
		super(Operator.NONE);
		this.value = value;
	}

	public ConstantTreeNode(String name, long value) {
		this(value);
		this.name = name;
	}

	@Override
	public ConstantTreeNode clone() {
		return new ConstantTreeNode(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
}
