package me.blog.hgl1002.openwnn.KOKR.event;

public class SetPropertyEvent extends Event {
	private String key;
	private Object value;

	public SetPropertyEvent(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}
}
