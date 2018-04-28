package io.github.lee0701.heonot.inputmethod.scripting.charcode;

public class H3Code extends HXCode {

	public H3Code(int cho, int jung, int jong) {
		super(cho, jung, jong);
		this.codeType = 0x0003;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new H3Code(cho, jung, jong);
	}
}
