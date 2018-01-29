package me.blog.hgl1002.openwnn.KOKR.event;

import java.util.List;

public abstract class Event {
	public static void fire(List<Listener> listeners, Event event) {
		for(Listener listener : listeners) {
			listener.onEvent(event);
		}
	}
}
