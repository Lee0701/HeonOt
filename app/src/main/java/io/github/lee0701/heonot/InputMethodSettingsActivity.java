package io.github.lee0701.heonot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.lee0701.heonot.KOKR.InputMethod;
import io.github.lee0701.heonot.KOKR.modules.InputMethodModule;

public class InputMethodSettingsActivity extends SettingsActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_method_settings);

		Intent intent = getIntent();
		InputMethod method = inputMethods.get(intent.getIntExtra(EXTRA_METHOD_ID, -1));

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.modules);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		for(InputMethodModule module : method.getModules()) {
			TextView textView = new TextView(this);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);
			textView.setText(module.getName());
			linearLayout.addView(textView);
			linearLayout.addView(module.createSettingsView(this));
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	}

}
