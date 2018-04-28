package io.github.lee0701.heonot.android.inputmethod.modules

import android.content.Context
import android.view.View
import io.github.lee0701.heonot.android.R

interface AndroidSettingsViewCreatable {

	fun createSettingsView(context: Context): View

}