package me.blog.hgl1002.openwnn.KOKR.event;

public class ComposeCharEvent extends Event {
	private String composingChar;

	public ComposeCharEvent(String composingChar) {
		this.composingChar = composingChar;
	}

	public String getComposingChar() {
		return composingChar;
	}
}
