package me.blog.hgl1002.openwnn;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import me.blog.hgl1002.openwnn.KOKR.scripting.StringRecursionTreeBuilder;
import me.blog.hgl1002.openwnn.KOKR.scripting.TreeBuilder;
import me.blog.hgl1002.openwnn.KOKR.scripting.TreeEvaluator;
import me.blog.hgl1002.openwnn.KOKR.scripting.nodes.TreeNode;

public class ExpressionTest {

	@Test
	public void test() {
		TreeBuilder builder = new StringRecursionTreeBuilder();
		TreeNode node = builder.build("A + B");
		Map<String, Long> variables = new HashMap<String, Long>() {{
			put("A", 1L);
			put("B", 2L);
		}};
		TreeEvaluator evaluator = new TreeEvaluator();
		evaluator.setVariables(variables);
		long result = evaluator.parse(node);
		System.out.println(result);
	}

}
