package io.github.lee0701.heonot.KOKR.event;

public interface EventListener {

	void onEvent(Event event);

	default int getPriority() {
		return 0;
	}

}
