package io.github.lee0701.heonot.inputmethod.modules;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.lee0701.heonot.inputmethod.event.SetPropertyEvent;

public abstract class InputMethodModule implements Cloneable {

	protected String name = "Module";

	public abstract void init();

	public View createSettingsView(Context context) {
		return new LinearLayout(context);
	}

	@Subscribe
	public void onSetProperty(SetPropertyEvent event) {
		this.setProperty(event.getKey(), event.getValue());
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

}
