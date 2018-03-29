package io.github.lee0701.heonot;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import io.github.lee0701.heonot.inputmethod.InputMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class SettingsActivity extends AppCompatActivity {

	static final String EXTRA_METHOD_ID = "io.github.lee0701.heonot.METHOD_ID";

	InputMethod globalModules;
	List<InputMethod> inputMethods = new ArrayList<>();

	File methodsDir;
	File globalMethodsFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		methodsDir = new File(getFilesDir(), "methods");
		globalMethodsFile = new File(getFilesDir(), "global.json");

		loadMethods();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_apply:
			saveMethods();
			Snackbar.make(findViewById(R.id.toolbar), R.string.msg_settings_saved, Snackbar.LENGTH_SHORT).show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void loadMethods() {
		inputMethods = InputMethodLoader.loadMethods(methodsDir);
		globalModules = InputMethodLoader.loadMethod(globalMethodsFile);
	}

	protected void saveMethods() {
		InputMethodLoader.storeMethods(methodsDir, inputMethods);
		InputMethodLoader.storeMethod(globalMethodsFile, globalModules);
		HeonOt instance = HeonOt.getInstance();
		if(instance != null) {
			instance.destroy();
			instance.init();
			instance.setInputView(instance.onCreateInputView());
		}
	}

}
