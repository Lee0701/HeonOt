package io.github.lee0701.heonot.inputmethod.scripting;

import java.util.Map;

import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode;

public interface TreeParser {
	void setConstants(Map<String, Long> constants);
	TreeNode parse(Object o);
}
