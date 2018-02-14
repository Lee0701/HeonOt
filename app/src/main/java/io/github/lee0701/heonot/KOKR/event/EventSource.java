package io.github.lee0701.heonot.KOKR.event;

import java.util.List;

public interface EventSource {

	void addListener(EventListener listener);

	void removeListener(EventListener listener);

	List<EventListener> getListeners();

}
