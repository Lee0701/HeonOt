package me.blog.hgl1002.openwnn.KOKR.event;

import java.util.List;

public abstract class Event {

	private boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public static void fire(List<Listener> listeners, Event event) {
		for(Listener listener : listeners) {
			listener.onEvent(event);
		}
	}
}
