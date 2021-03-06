package io.github.lee0701.heonot.inputmethod.scripting.nodes;

import java.util.Arrays;
import java.util.List;

public class ListTreeNode extends TreeNode {

	private List<TreeNode> nodes;

	public ListTreeNode(Operator operator, List<TreeNode> nodes) {
		super(operator);
		this.nodes = nodes;
	}

	public ListTreeNode(Operator operator, TreeNode... nodes) {
		super(operator);
		this.nodes = Arrays.asList(nodes);
	}

	@Override
	public ListTreeNode clone() {
		return new ListTreeNode(getOperator(), nodes);
	}

	public List<TreeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
}
