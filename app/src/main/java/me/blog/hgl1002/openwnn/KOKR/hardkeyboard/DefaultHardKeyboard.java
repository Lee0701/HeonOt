package me.blog.hgl1002.openwnn.KOKR.hardkeyboard;

import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.blog.hgl1002.openwnn.KOKR.event.Event;
import me.blog.hgl1002.openwnn.KOKR.event.InputCharEvent;
import me.blog.hgl1002.openwnn.KOKR.event.InputRawEvent;
import me.blog.hgl1002.openwnn.KOKR.event.KeyPressEvent;
import me.blog.hgl1002.openwnn.KOKR.event.Listener;
import me.blog.hgl1002.openwnn.KOKR.generator.CharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.generator.UnicodeJamoHandler;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.def.DefaultHardKeyboardMap;

public class DefaultHardKeyboard implements HardKeyboard {

	List<Listener> listeners = new ArrayList<>();

	String layoutJson;

	private Map<Integer, DefaultHardKeyboardMap> table;

	private Map<UnicodeJamoHandler.JamoPair, Character> combinationTable;

	public DefaultHardKeyboard() {

	}

	public DefaultHardKeyboard(String layoutJson) {
		this.layoutJson = layoutJson;
	}

	private void loadLayout(JSONObject layout) throws JSONException {

		this.table = new HashMap<>();

		JSONArray table = layout.getJSONArray("table");
		JSONArray combination = layout.getJSONArray("combination");

		if(table != null) {
			for(int i = 0 ; i < table.length() ; i++) {
				JSONObject o = table.getJSONObject(i);

				int keyCode = o.getInt("keycode");
				String normal = o.getString("normal");
				String shift = o.getString("shift");

				DefaultHardKeyboardMap map = new DefaultHardKeyboardMap(keyCode, Integer.parseInt(normal), Integer.parseInt(shift));
				this.table.put(keyCode, map);

			}
		}

		if(combination != null) {
			for(int i = 0 ; i < combination.length() ; i++) {
				JSONObject o = table.getJSONObject(i);
				int a = o.getInt("a");
				int b = o.getInt("b");
				String result = o.getString("result");
				UnicodeJamoHandler.JamoPair pair = new UnicodeJamoHandler.JamoPair((char) a, (char) b);
				combinationTable.put(pair, (char) Integer.parseInt(result));
			}
		}

	}

	@Override
	public void init() {
		try {
			if(layoutJson != null) this.loadLayout(new JSONObject(layoutJson));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(Event e) {
		if(e instanceof KeyPressEvent) {
			KeyPressEvent event = (KeyPressEvent) e;
			this.input(event);
		}
	}

	@Override
	public void input(KeyPressEvent event) {
		if(table == null) {
			Event.fire(listeners, new InputRawEvent(event));
			return;
		}
		DefaultHardKeyboardMap map = table.get(event.getKeyCode());
		if(map != null) {
			int charCode = event.isShift() ? map.getShift() : map.getNormal();
			Event.fire(listeners, new InputCharEvent(charCode));
		}
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
}
