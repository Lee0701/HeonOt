package io.github.lee0701.heonot.inputmethod;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;
import io.github.lee0701.heonot.inputmethod.modules.softkeyboard.SoftKeyboard;
import io.github.lee0701.heonot.HeonOt;

public class InputMethod {

	private String name = "";

	private List<InputMethodModule> modules;

	private InputMethod(InputMethodModule... modules) {
		this.modules = new ArrayList<>(Arrays.asList(modules));
	}

	public InputMethod(InputMethod original) {
		setName(original.getName());
		List<InputMethodModule> list = new ArrayList<>();
		for (InputMethodModule inputMethodModule : original.getModules()) {
			list.add((InputMethodModule) inputMethodModule.clone());
		}
		this.modules = list;
	}

	public void registerListeners() {
		for(InputMethodModule module : modules) {
			EventBus.getDefault().register(module);
		}
	}

	public void clearListeners() {
		for(InputMethodModule module : modules) {
			EventBus.getDefault().unregister(module);
		}
	}

	public void init() {
		for(InputMethodModule module : modules) {
			module.init();
		}
	}

	public void pause() {
		for(InputMethodModule module : modules) {
			module.pause();
			EventBus.getDefault().unregister(module);
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
					JSONObject props = module.optJSONObject("properties");
					if(props != null && props.names() != null) {
						JSONArray names = props.names();
						for(int j = 0 ; j < names.length() ; j++) {
							String key = names.getString(j);
							m.setProperty(key, props.opt(key));
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
	@Deprecated
	/**
	 * It is deprecated. Use copy constructor instead.
	 */
	public Object clone() {
		return new InputMethod(this);
	}
}
