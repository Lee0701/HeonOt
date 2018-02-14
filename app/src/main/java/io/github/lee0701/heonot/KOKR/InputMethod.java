package io.github.lee0701.heonot.KOKR;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.lee0701.heonot.KOKR.modules.InputMethodModule;
import io.github.lee0701.heonot.KOKR.modules.softkeyboard.SoftKeyboard;
import io.github.lee0701.heonot.KOKR.modules.generator.CharacterGenerator;
import io.github.lee0701.heonot.KOKR.modules.hardkeyboard.HardKeyboard;
import io.github.lee0701.heonot.HeonOt;

public class InputMethod {

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

}
