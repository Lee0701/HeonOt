package me.blog.hgl1002.openwnn.KOKR.event;

public class SoftKeyPressEvent extends Event {
	private final int keyCode;

	public SoftKeyPressEvent(int keyCode) {
		this.keyCode = keyCode;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public static class SoftKeyReleaseEvent extends SoftKeyPressEvent {
		public SoftKeyReleaseEvent(int keyCode) {
			super(keyCode);
		}
	}

}
