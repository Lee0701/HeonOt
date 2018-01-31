package me.blog.hgl1002.openwnn.KOKR.scripting;

import java.util.Map;

import me.blog.hgl1002.openwnn.KOKR.scripting.nodes.TreeNode;

public interface TreeBuilder {
	public void setConstants(Map<String, Long> constants);
	public TreeNode build(Object o);
}
