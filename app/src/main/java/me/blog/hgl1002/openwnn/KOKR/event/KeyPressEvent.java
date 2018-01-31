package me.blog.hgl1002.openwnn.KOKR.event;

public class KeyPressEvent extends Event {
	private final int keyCode;
	private final int repeated;
	private final int metaState;

	public KeyPressEvent(int keyCode, int metaState, int repeated) {
		this.keyCode = keyCode;
		this.metaState = metaState;
		this.repeated = repeated;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getMetaState() {
		return metaState;
	}

	public int getRepeated() {
		return repeated;
	}

	public static class KeyReleaseEvent extends KeyPressEvent {
		public KeyReleaseEvent(int keyCode, int metaState, int repeated) {
			super(keyCode, metaState, repeated);
		}
	}

}
