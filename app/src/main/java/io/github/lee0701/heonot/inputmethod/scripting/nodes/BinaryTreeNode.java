package io.github.lee0701.heonot.inputmethod.scripting.nodes;

public class BinaryTreeNode extends TreeNode {

	private TreeNode left, right;

	public BinaryTreeNode(int operator, TreeNode left, TreeNode right) {
		super(operator);
		this.left = left;
		this.right = right;
	}

	@Override
	public BinaryTreeNode clone() {
		return new BinaryTreeNode(getOperator(), left, right);
	}

	public TreeNode getLeft() {
		return left;
	}

	public void setLeft(TreeNode left) {
		this.left = left;
	}

	public TreeNode getRight() {
		return right;
	}

	public void setRight(TreeNode right) {
		this.right = right;
	}
}
