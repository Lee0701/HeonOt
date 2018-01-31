package me.blog.hgl1002.openwnn.KOKR.scripting.charcode;

public class H2Code extends HXCode {

	public H2Code(int cho, int jung, int jong) {
		super(cho, jung, jong);
		this.codeType = 0x0002;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new H2Code(cho, jung, jong);
	}
}
