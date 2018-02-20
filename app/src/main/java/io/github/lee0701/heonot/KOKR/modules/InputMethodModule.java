package io.github.lee0701.heonot.KOKR.modules;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.EventSource;

public abstract class InputMethodModule implements EventListener, EventSource, Cloneable {

	protected String name = "Module";

	List<EventListener> listeners = new ArrayList<>();

	public abstract void init();

	public View createSettingsView(Context context) {
		return new LinearLayout(context);
	}

	public void setProperty(String key, Object value) {
	}

	@Override
	public abstract Object clone();

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
