package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public class UnaryTreeNode extends TreeNode {

	private TreeNode center;

	public UnaryTreeNode(Operator operator, TreeNode center) {
		super(operator);
		this.center = center;
	}

	@Override
	public UnaryTreeNode clone() {
		return new UnaryTreeNode(getOperator(), center);
	}

	public TreeNode getCenter() {
		return center;
	}

	public void setCenter(TreeNode center) {
		this.center = center;
	}
}
