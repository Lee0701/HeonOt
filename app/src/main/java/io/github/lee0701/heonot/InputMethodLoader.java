package io.github.lee0701.heonot;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.inputmethod.InputMethod;

public class InputMethodLoader {

	public static InputMethod loadMethod(File methodFile) {
		try(FileInputStream fis = new FileInputStream(methodFile)) {
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			InputMethod method = InputMethod.loadJSON(new String(bytes));
			return method;
		} catch(IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void storeMethod(File methodFile, InputMethod method) {
		try(FileOutputStream fos = new FileOutputStream(methodFile)) {
			fos.write(method.toJSON(-1).getBytes());
		} catch(IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	public static List<InputMethod> loadMethods(File methodsDir) {
		List<InputMethod> inputMethods = new ArrayList<>();
		for(File file : methodsDir.listFiles()) {
			if(file.getName().endsWith(".json")) {
				String fileName = file.getName().replace(".json", "");
				try {
					int index = Integer.parseInt(fileName);
					InputMethod method = loadMethod(file);
					if(method != null) inputMethods.add(index, method);
				} catch(NumberFormatException e) {
					continue;
				}
			}
		}
		return inputMethods;
	}

	public static void storeMethods(File methodsDir, List<InputMethod> inputMethods) {
		for(File file : methodsDir.listFiles()) {
			file.delete();
		}
		for(int i = 0 ; i < inputMethods.size() ; i++) {
			InputMethod method = inputMethods.get(i);
			File file = new File(methodsDir, i + ".json");
			storeMethod(file, method);
		}
	}

}
