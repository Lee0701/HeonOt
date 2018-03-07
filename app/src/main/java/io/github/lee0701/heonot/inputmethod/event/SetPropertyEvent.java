package io.github.lee0701.heonot.inputmethod.event;

public final class SetPropertyEvent extends Event {
	private final String key;
	private final Object value;

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
