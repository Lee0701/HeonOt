package io.github.lee0701.heonot.KOKR.scripting.charcode;

public abstract class HXCode extends CharacterCode {

	protected int cho, jung, jong;

	public HXCode(int cho, int jung, int jong) {
		this.cho = cho;
		this.jung = jung;
		this.jong = jong;
	}

	public HXCode() {
		this(0, 0, 0);
	}

	public static HXCode fromUnicode() {
		//TODO
		return null;
	}

	public char toUnicode() {
		//TODO
		return 0;
	}

	public int getCho() {
		return cho;
	}

	public void setCho(int cho) {
		this.cho = cho;
	}

	public int getJung() {
		return jung;
	}

	public void setJung(int jung) {
		this.jung = jung;
	}

	public int getJong() {
		return jong;
	}

	public void setJong(int jong) {
		this.jong = jong;
	}
}
