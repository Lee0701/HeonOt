package me.blog.hgl1002.openwnn.KOKR.event;

public class InputCharEvent extends Event {
	private Object character;

	public InputCharEvent(Object character) {
		this.character = character;
	}

	public Object getCharacter() {
		return character;
	}
}
