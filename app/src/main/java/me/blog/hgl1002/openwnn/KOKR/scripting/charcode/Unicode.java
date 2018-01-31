package me.blog.hgl1002.openwnn.KOKR.scripting.charcode;

public class Unicode extends CharacterCode {

	protected char code;

	public Unicode(char code) {
		this.code = code;
		this.codeType = 1;
	}

	public Unicode() {
		this((char) 0);
	}

	public char getCode() {
		return code;
	}

	public void setCode(char code) {
		this.code = code;
	}
}
