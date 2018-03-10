package io.github.lee0701.heonot.inputmethod.modules;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.lee0701.heonot.R;
import io.github.lee0701.heonot.inputmethod.event.SetPropertyEvent;

public abstract class InputMethodModule implements Cloneable {

	protected String name = "Module";

	public abstract void init();

	public abstract void pause();

	public View createSettingsView(Context context) {
		LinearLayout settings = new LinearLayout(context);

		TextView nameView = new TextView(context);
		nameView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);
		nameView.setText(getName());
		settings.addView(nameView);

		nameView.setOnClickListener((v) -> {
			EditText nameEdit = new EditText(context);
			nameEdit.setText(getName());
			new AlertDialog.Builder(context)
					.setTitle(R.string.msg_module_name)
					.setView(nameEdit)
					.setPositiveButton(R.string.button_ok, (dialog, which) -> {
						setName(nameEdit.getText().toString());
						nameView.setText(getName());
					})
					.setNegativeButton(R.string.button_cancel, (dialog, which) -> {})
					.create()
			.show();
		});

		return settings;
	}

	@Subscribe
	public void onSetProperty(SetPropertyEvent event) {
		this.setProperty(event.getKey(), event.getValue());
	}

	public void setProperty(String key, Object value) {
	}

	@Override
	public abstract InputMethodModule clone();

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
