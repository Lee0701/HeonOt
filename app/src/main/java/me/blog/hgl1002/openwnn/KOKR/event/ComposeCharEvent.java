package me.blog.hgl1002.openwnn.KOKR.event;

public class ComposeCharEvent extends Event {
	private String composingChar;
	private int lastInput;

	public ComposeCharEvent(String composingChar, int lastInput) {
		this.composingChar = composingChar;
		this.lastInput = lastInput;
	}

	public String getComposingChar() {
		return composingChar;
	}

	public int getLastInput() {
		return lastInput;
	}
}
