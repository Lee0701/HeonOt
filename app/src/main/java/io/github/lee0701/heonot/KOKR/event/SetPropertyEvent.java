package io.github.lee0701.heonot.KOKR.event;

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
