package io.github.lee0701.heonot;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import io.github.lee0701.heonot.inputmethod.InputMethod;
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;

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

		RelativeLayout relativeLayout = new RelativeLayout(this);

		Button exportButton = new Button(this);
		exportButton.setText(R.string.button_export);
		exportButton.setOnClickListener((v) -> {
			String json;
			try {
				json = method.toJSON(2);
			} catch(JSONException e) {
				return;
			}
			new AlertDialog.Builder(this)
					.setMessage(R.string.msg_copy_method_json)
					.setPositiveButton(R.string.button_ok, (dialog, which) -> {
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						if(Build.VERSION.SDK_INT > 11) {
							ClipData clipData = ClipData.newPlainText("JSON Data", json);
							clipboard.setPrimaryClip(clipData);
						} else {
							clipboard.setText(json);
						}
					})
					.setNegativeButton(R.string.button_cancel, (dialog, which) -> {})
					.create()
			.show();
		});
		relativeLayout.addView(exportButton);

		linearLayout.addView(relativeLayout);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	}

}
