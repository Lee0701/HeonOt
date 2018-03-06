package io.github.lee0701.heonot.inputmethod.scripting;

import java.util.Map;

import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode;

public interface TreeBuilder {
	void setConstants(Map<String, Long> constants);
	TreeNode build(Object o);
}
