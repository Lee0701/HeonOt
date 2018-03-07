package io.github.lee0701.heonot.inputmethod.event;

public final class DeleteCharEvent extends Event {
	private final int beforeLength;
	private final int afterLength;

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
