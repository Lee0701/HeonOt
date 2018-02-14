package io.github.lee0701.heonot.KOKR.event;

import java.util.List;

public interface EventSource {

	void addListener(EventListener listener);

	void removeListener(EventListener listener);

	void clearListeners();

	List<EventListener> getListeners();

}
