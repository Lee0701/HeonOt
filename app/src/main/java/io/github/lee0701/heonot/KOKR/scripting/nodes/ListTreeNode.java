package io.github.lee0701.heonot.KOKR.scripting.nodes;

import java.util.Arrays;
import java.util.List;

public class ListTreeNode extends TreeNode {

	List<TreeNode> nodes;

	public ListTreeNode(int operator, List<TreeNode> nodes) {
		super(operator);
		this.nodes = nodes;
	}

	public ListTreeNode(int operator, TreeNode... nodes) {
		super(operator);
		this.nodes = Arrays.asList(nodes);
	}

	public List<TreeNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
}
