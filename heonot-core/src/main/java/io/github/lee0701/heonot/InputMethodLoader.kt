package io.github.lee0701.heonot

import org.json.JSONException

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

import io.github.lee0701.heonot.inputmethod.InputMethod

object InputMethodLoader {

	fun loadMethod(methodFile: File): InputMethod? {
		try {
			FileInputStream(methodFile).use { fis ->
				val bytes = ByteArray(fis.available())
				fis.read(bytes)
				return InputMethod.loadJSON(String(bytes))
			}
		} catch (e: IOException) {
			e.printStackTrace()
			return null
		} catch (e: JSONException) {
			e.printStackTrace()
			return null
		}

	}

	fun storeMethod(methodFile: File, method: InputMethod) {
		try {
			FileOutputStream(methodFile).use { fos -> fos.write(method.toJSON(-1).toByteArray()) }
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: JSONException) {
			e.printStackTrace()
		}

	}

	fun loadMethods(methodsDir: File): MutableList<InputMethod> {
		val inputMethods = mutableListOf<InputMethod>()
		for (file in methodsDir.listFiles()!!) {
			if (file.name.endsWith(".json")) {
				val fileName = file.name.replace(".json", "")
				try {
					val index = Integer.parseInt(fileName)
					val method = loadMethod(file)
					if (method != null) inputMethods.add(index, method)
				} catch (e: NumberFormatException) {
					continue
				}

			}
		}
		return inputMethods
	}

	fun storeMethods(methodsDir: File, inputMethods: List<InputMethod>) {
		for (file in methodsDir.listFiles()!!) {
			file.delete()
		}
		for (i in inputMethods.indices) {
			val method = inputMethods[i]
			val file = File(methodsDir, i.toString() + ".json")
			storeMethod(file, method)
		}
	}

}
