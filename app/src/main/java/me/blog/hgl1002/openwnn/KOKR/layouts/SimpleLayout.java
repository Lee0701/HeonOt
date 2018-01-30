package me.blog.hgl1002.openwnn.KOKR.layouts;

import java.util.Map;

public class SimpleLayout implements Layout {

	protected Map<Integer, KeyMap> jamoMap;

	@Override
	public long getJamo(int code, boolean shift) {
		if(jamoMap == null) return 0;
		KeyMap mapping = jamoMap.get(code);
		if(mapping == null) return 0;
		try {
			return shift ? mapping.get(1) : mapping.get(0);
		} catch(ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}

	public static class KeyMap {
		private final int keyCode;
		private final long[] codes;

		public KeyMap(int keyCode, long... codes) {
			this.keyCode = keyCode;
			this.codes = codes;
		}

		public int getKeyCode() {
			return keyCode;
		}

		public long[] getCodes() {
			return codes;
		}

		public long get(int i) {
			return codes[i];
		}

	}

}
