package io.github.lee0701.heonot.KOKR.softkeyboard;

import android.content.Context;
import android.view.View;

import io.github.lee0701.heonot.KOKR.event.EventListener;
import io.github.lee0701.heonot.KOKR.event.EventSource;

public interface SoftKeyboard extends EventListener, EventSource {

	void init();

	View createView(Context context);

}
