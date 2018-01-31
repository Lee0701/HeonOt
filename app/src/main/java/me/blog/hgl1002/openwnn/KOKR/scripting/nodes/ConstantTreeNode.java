package me.blog.hgl1002.openwnn.KOKR.scripting.nodes;

public class ConstantTreeNode extends TreeNode {

	long value;

	public ConstantTreeNode(long value) {
		super(Operator.NONE);
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
}
