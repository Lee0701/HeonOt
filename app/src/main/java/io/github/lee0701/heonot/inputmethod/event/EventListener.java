package io.github.lee0701.heonot.inputmethod.event;

public interface EventListener {

	void onEvent(Event event);

	default int getPriority() {
		return 0;
	}

}
