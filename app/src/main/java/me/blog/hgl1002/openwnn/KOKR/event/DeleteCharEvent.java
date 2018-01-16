package me.blog.hgl1002.openwnn.KOKR.event;

public class DeleteCharEvent extends Event {
	private int beforeLength, afterLength;

	public DeleteCharEvent(int beforeLength, int afterLength) {
		this.beforeLength = beforeLength;
		this.afterLength = afterLength;
	}

	public int getBeforeLength() {
		return beforeLength;
	}

	public int getAfterLength() {
		return afterLength;
	}
}
