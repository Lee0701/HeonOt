package me.blog.hgl1002.openwnn;

import org.json.JSONObject;
import org.junit.Test;

import me.blog.hgl1002.openwnn.KOKR.EngineMode;

public class LayoutConverterTest {
	@Test
	public void test() {
		String converted = new LayoutConverter(EngineMode.SEBUL_391).convert();
		System.out.println(converted);
	}

}
