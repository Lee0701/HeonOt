package io.github.lee0701.heonot.KOKR.event;

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
