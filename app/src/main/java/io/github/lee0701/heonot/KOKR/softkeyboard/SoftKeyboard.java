package io.github.lee0701.heonot.KOKR.softkeyboard;

import android.content.Context;
import android.view.View;

import io.github.lee0701.heonot.KOKR.event.Listener;

public interface SoftKeyboard extends Listener {

	void init();

	View createView(Context context);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
