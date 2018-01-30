package me.blog.hgl1002.openwnn.KOKR.event;

public class InputRawEvent extends Event {
	private KeyPressEvent event;

	public InputRawEvent(KeyPressEvent event) {
		this.event = event;
	}

	public KeyPressEvent getEvent() {
		return event;
	}
}
