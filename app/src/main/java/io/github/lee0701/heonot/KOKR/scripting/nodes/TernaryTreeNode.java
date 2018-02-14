package io.github.lee0701.heonot.KOKR.scripting.nodes;

public class TernaryTreeNode extends TreeNode {

	TreeNode left, center, right;

	public TernaryTreeNode(int operator, TreeNode left, TreeNode center, TreeNode right) {
		super(operator);
		this.left = left;
		this.center = center;
		this.right = right;
	}

	public TreeNode getLeft() {
		return left;
	}

	public void setLeft(TreeNode left) {
		this.left = left;
	}

	public TreeNode getCenter() {
		return center;
	}

	public void setCenter(TreeNode center) {
		this.center = center;
	}

	public TreeNode getRight() {
		return right;
	}

	public void setRight(TreeNode right) {
		this.right = right;
	}
}
