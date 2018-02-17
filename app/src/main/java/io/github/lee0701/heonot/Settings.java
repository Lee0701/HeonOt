package io.github.lee0701.heonot;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.KOKR.InputMethod;

public class Settings extends AppCompatActivity {

	HeonOt heonOt;

	List<InputMethod> inputMethods = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		heonOt = HeonOt.getInstance();
		if(heonOt == null) {
			heonOt = new HeonOt(this);
			heonOt.onCreate();
		}
		inputMethods.addAll(heonOt.getInputMethods());

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ListView listView = (ListView) findViewById(R.id.method_list);

		ListViewAdapter adapter = new ListViewAdapter(this, inputMethods);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				InputMethod method = inputMethods.get(position);
				final EditText content = new EditText(Settings.this);
				try {
					content.setText(method.toJSON(2));
				} catch(JSONException e) {
					e.printStackTrace();
					Toast.makeText(Settings.this, e.getClass().getName() + ":  " + e.getMessage(), Toast.LENGTH_LONG).show();
					return;
				}
				new AlertDialog.Builder(Settings.this, R.style.Theme_AppCompat_Light)
						.setTitle(method.getName())
						.setView(content)
						.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									inputMethods.set(position, InputMethod.loadJSON(content.getText().toString()));
								} catch(JSONException e) {
									e.printStackTrace();
									Toast.makeText(Settings.this, e.getClass().getName() + ":  " + e.getMessage(), Toast.LENGTH_LONG).show();
								}
							}
						})
						.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						})
						.create().show();
			}
		});

	}

	private static class ListViewAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private List<InputMethod> data;

		public ListViewAdapter(Context context, List<InputMethod> data) {
			this.inflater = LayoutInflater.from(context);
			this.data = data;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position).getName();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = inflater.inflate(R.layout.item, parent, false);
			}
			InputMethod method = data.get(position);
			TextView text = (TextView) convertView.findViewById(R.id.text);
			text.setText(position + ": " + method.getName());
			text.setTextSize(TypedValue.COMPLEX_UNIT_PT, 12);

			return convertView;
		}
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
			heonOt.setInputMethods(inputMethods);
			heonOt.setInputView(heonOt.onCreateInputView());
			File methodsDir = new File(getFilesDir(), "methods");
			heonOt.storeMethods(methodsDir);
			Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
