package io.github.lee0701.heonot.KOKR.event;

import java.util.List;

public abstract class Event {

	private EventSource source;

	private boolean cancelled;

	public EventSource getSource() {
		return source;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static void fire(EventSource source, Event event) {
		event.source = source;
		for(EventListener listener : source.getListeners()) {
			listener.onEvent(event);
		}
	}

}
