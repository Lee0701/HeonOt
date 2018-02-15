package io.github.lee0701.heonot.KOKR.modules;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.EventSource;

public abstract class InputMethodModule implements EventListener, EventSource {

	protected String name = "Module";

	List<EventListener> listeners = new ArrayList<>();

	public abstract void init();

	public void setProperty(String key, Object value) {
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("name", this.name);
		object.put("class", this.getClass().getName());
		return object;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void clearListeners() {
		listeners.clear();
	}

	@Override
	public List<EventListener> getListeners() {
		return listeners;
	}

}
