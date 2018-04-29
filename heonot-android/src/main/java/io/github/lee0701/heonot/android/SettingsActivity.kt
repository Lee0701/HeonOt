package io.github.lee0701.heonot.android

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import org.greenrobot.eventbus.EventBus

import io.github.lee0701.heonot.HeonOt
import io.github.lee0701.heonot.InputMethodLoader
import io.github.lee0701.heonot.android.R
import io.github.lee0701.heonot.inputmethod.InputMethod
import io.github.lee0701.heonot.inputmethod.event.CreateViewEvent

import java.io.File

abstract class SettingsActivity : AppCompatActivity() {

	var globalModules: InputMethod = InputMethod(mutableListOf())
	var inputMethods = mutableListOf<InputMethod>()

	var methodsDir: File? = null
	var globalMethodsFile: File? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		methodsDir = File(filesDir, "methods")
		globalMethodsFile = File(filesDir, "global.json")

		HeonOt.INSTANCE ?: AndroidHeonOt(this)

		loadMethods()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.settings, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_apply -> {
				saveMethods()
				Snackbar.make(findViewById(R.id.toolbar), R.string.msg_settings_saved, Snackbar.LENGTH_SHORT).show()
				return true
			}

			else -> return super.onOptionsItemSelected(item)
		}
	}

	protected open fun loadMethods() {
		inputMethods = InputMethodLoader.loadMethods(methodsDir!!)
		globalModules = InputMethodLoader.loadMethod(globalMethodsFile!!) ?: globalModules
	}

	protected fun saveMethods() {
		InputMethodLoader.storeMethods(methodsDir!!, inputMethods)
		InputMethodLoader.storeMethod(globalMethodsFile!!, globalModules!!)
		val instance = HeonOt.INSTANCE
		if (instance != null) {
			instance.destroy()
			instance.init()
			EventBus.getDefault().post(CreateViewEvent())
		}
	}

	companion object {
		internal val EXTRA_METHOD_ID = "io.github.lee0701.heonot.METHOD_ID"
	}

}
