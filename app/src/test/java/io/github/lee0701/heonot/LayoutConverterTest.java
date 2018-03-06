package io.github.lee0701.heonot;

import org.junit.Test;

import io.github.lee0701.heonot.inputmethod.EngineMode;

public class LayoutConverterTest {
	@Test
	public void test() {
		String converted = new LayoutConverter(EngineMode.SEBUL_391).convert();
		System.out.println(converted);
	}

}
