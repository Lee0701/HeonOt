package io.github.lee0701.heonot.KOKR;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.lee0701.heonot.KOKR.modules.InputMethodModule;
import io.github.lee0701.heonot.KOKR.modules.softkeyboard.SoftKeyboard;
import io.github.lee0701.heonot.HeonOt;

public class InputMethod implements Cloneable {

	private String name = "";

	private List<InputMethodModule> modules;

	public InputMethod(InputMethodModule... modules) {
		this.modules = new ArrayList<>();
		this.modules.addAll(Arrays.asList(modules));
	}

	public void registerListeners(HeonOt parent) {
		for(InputMethodModule module : modules) {
			parent.addListener(module);
			module.addListener(parent);
			for(InputMethodModule listener : modules) {
				if(module != listener) module.addListener(listener);
			}
		}
	}

	public void clearListeners() {
		for(InputMethodModule module : modules) {
			module.clearListeners();
		}
	}

	public void init() {
		for(InputMethodModule module : modules) {
			module.init();
		}
	}

	public View createView(Context context) {
		LinearLayout view = new LinearLayout(context);
		for(InputMethodModule module : modules) {
			if(module instanceof SoftKeyboard) {
				view.addView(((SoftKeyboard) module).createView(context));
			}
		}
		return view;
	}

	public String toJSON(int indentSpaces) throws JSONException {
		JSONObject methodObject = new JSONObject();

		methodObject.put("name", getName());

		JSONArray modules = new JSONArray();

		for(InputMethodModule module : this.modules) {
			modules.put(module.toJSONObject());
		}

		methodObject.put("modules", modules);
		if(indentSpaces > 0) return methodObject.toString(indentSpaces);
		else return methodObject.toString();
	}

	public static InputMethod loadJSON(String methodJson) throws JSONException {
		JSONObject methodObject = new JSONObject(methodJson);

		JSONArray modulesArray = methodObject.optJSONArray("modules");

		List<InputMethodModule> modules = new ArrayList<>();
		if(modulesArray != null) {
			for(int i = 0 ; i < modulesArray.length() ; i++) {
				JSONObject module = modulesArray.getJSONObject(i);
				String name = module.optString("name");
				String className = module.getString("class");
				try {
					Class<?> moduleClass = Class.forName(className);
					InputMethodModule m = (InputMethodModule) moduleClass.getDeclaredConstructor().newInstance();
					m.setName(name);
					JSONArray props = module.optJSONArray("properties");
					if(props != null) {
						for(int j = 0 ; j < props.length() ; j++) {
							JSONObject prop = props.getJSONObject(j);
							String key = prop.optString("key", null);
							Object value = prop.opt("value");
							if(key != null) m.setProperty(key, value);
						}
					}
					modules.add(m);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		InputMethodModule[] arr = new InputMethodModule[modules.size()];
		InputMethod method = new InputMethod(modules.toArray(arr));
		method.setName(methodObject.optString("name"));

		return method;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<InputMethodModule> getModules() {
		return modules;
	}

	public void setModules(List<InputMethodModule> modules) {
		this.modules = modules;
	}

	@Override
	public Object clone() {
		InputMethodModule[] modules = new InputMethodModule[this.modules.size()];
		for(int i = 0 ; i < modules.length ; i++) {
			modules[i] = (InputMethodModule) this.modules.get(i).clone();
		}
		InputMethod cloned = new InputMethod(modules);
		cloned.setName(getName());
		return cloned;
	}
}
