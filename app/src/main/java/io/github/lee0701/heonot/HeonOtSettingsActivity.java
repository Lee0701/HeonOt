package io.github.lee0701.heonot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;

import io.github.lee0701.heonot.KOKR.InputMethod;

public class HeonOtSettingsActivity extends SettingsActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heonot_settings);

		ListView listView = (ListView) findViewById(R.id.method_list);

		ListViewAdapter adapter = new ListViewAdapter(this, inputMethods);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				InputMethod method = inputMethods.get(position);
				final EditText content = new EditText(HeonOtSettingsActivity.this);
				try {
					content.setText(method.toJSON(2));
				} catch(JSONException e) {
					e.printStackTrace();
					Toast.makeText(HeonOtSettingsActivity.this, e.getClass().getName() + ":  " + e.getMessage(), Toast.LENGTH_LONG).show();
					return;
				}
				Intent intent = new Intent(HeonOtSettingsActivity.this, InputMethodSettingsActivity.class);
				intent.putExtra(EXTRA_METHOD_ID, position);
				startActivity(intent);
			}
		});

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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

}
