package io.github.lee0701.heonot;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;

import io.github.lee0701.heonot.inputmethod.InputMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SettingsActivity extends AppCompatActivity {

	static final String EXTRA_METHOD_ID = "io.github.lee0701.heonot.METHOD_ID";

	List<InputMethod> inputMethods = new ArrayList<>();

	File methodsDir;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		methodsDir = new File(getFilesDir(), "methods");

		loadMethods(methodsDir);
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

			storeMethods(methodsDir);
			HeonOt instance = HeonOt.getInstance();
			if(instance != null) {
				instance.destroy();
				instance.init();
				instance.setInputView(instance.onCreateInputView());
			}
			Snackbar.make(findViewById(R.id.toolbar), R.string.settings_saved, Snackbar.LENGTH_SHORT).show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void loadMethods(File methodsDir) {
		inputMethods.clear();
		for(File file : methodsDir.listFiles()) {
			if(file.getName().endsWith(".json")) {
				String fileName = file.getName().replace(".json", "");
				int index = Integer.parseInt(fileName);
				try(FileInputStream fis = new FileInputStream(file)) {
					byte[] bytes = new byte[fis.available()];
					fis.read(bytes);
					InputMethod method = InputMethod.loadJSON(new String(bytes));
					inputMethods.add(index, method);
				} catch(IOException | JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void storeMethods(File methodsDir) {
		for(int i = 0 ; i < inputMethods.size() ; i++) {
			InputMethod method = inputMethods.get(i);
			File file = new File(methodsDir, i + ".json");
			try(FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(method.toJSON(-1).getBytes());
			} catch(IOException | JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
