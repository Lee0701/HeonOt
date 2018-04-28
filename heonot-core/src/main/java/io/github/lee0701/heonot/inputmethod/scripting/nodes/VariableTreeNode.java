package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public class VariableTreeNode extends TreeNode {

	private String name;

	public VariableTreeNode(String name) {
		super(Operator.NONE);
		this.name = name;
	}

	@Override
	public VariableTreeNode clone() {
		return new VariableTreeNode(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
