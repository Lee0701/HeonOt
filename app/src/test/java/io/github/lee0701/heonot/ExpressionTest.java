package io.github.lee0701.heonot;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import io.github.lee0701.heonot.inputmethod.scripting.StringRecursionTreeParser;
import io.github.lee0701.heonot.inputmethod.scripting.TreeParser;
import io.github.lee0701.heonot.inputmethod.scripting.TreeEvaluator;
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode;

public class ExpressionTest {

	@Test
	public void test() {
		TreeParser builder = new StringRecursionTreeParser();
		TreeNode node = builder.parse("A << 4");
		Map<String, Long> variables = new HashMap<String, Long>() {{
			put("A", 1L);
			put("B", 2L);
		}};
		TreeEvaluator evaluator = new TreeEvaluator();
		evaluator.setVariables(variables);
		long result = evaluator.eval(node);
		System.out.println(result);
	}

}
