package io.github.lee0701.heonot.android.inputmethod.modules

import android.content.Context
import android.view.View

interface AndroidViewCreatable {
	fun createView(context: Context) : View
}