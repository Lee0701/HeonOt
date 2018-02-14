package io.github.lee0701.heonot.KOKR.scripting.nodes;

public class UnaryTreeNode extends TreeNode {

	TreeNode center;

	public UnaryTreeNode(int operator, TreeNode center) {
		super(operator);
		this.center = center;
	}

	public TreeNode getCenter() {
		return center;
	}

	public void setCenter(TreeNode center) {
		this.center = center;
	}
}
