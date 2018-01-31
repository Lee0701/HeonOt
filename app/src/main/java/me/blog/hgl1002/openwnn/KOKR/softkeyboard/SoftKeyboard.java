package me.blog.hgl1002.openwnn.KOKR.softkeyboard;

import android.content.Context;
import android.view.View;

import me.blog.hgl1002.openwnn.KOKR.event.Listener;

public interface SoftKeyboard {

	void init();

	View createView(Context context);

	void addListener(Listener listener);

	void removeListener(Listener listener);

}
