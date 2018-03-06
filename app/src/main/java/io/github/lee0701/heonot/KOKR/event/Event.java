package io.github.lee0701.heonot.KOKR.event;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
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
		List<EventListener> listeners = new ArrayList<>(source.getListeners());
		Collections.sort(listeners, (o1, o2) -> o2.getPriority()-o1.getPriority());
		for(EventListener listener : listeners) {
			listener.onEvent(event);
			if(event.isCancelled()) break;
		}
	}

}
