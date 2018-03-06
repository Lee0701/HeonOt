package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public class VariableTreeNode extends TreeNode {

	String name;

	public VariableTreeNode(String name) {
		super(Operator.NONE);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
