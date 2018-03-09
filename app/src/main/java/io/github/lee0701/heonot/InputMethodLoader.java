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

	public static List<InputMethod> loadMethods(File methodsDir) {
		List<InputMethod> inputMethods = new ArrayList<>();
		for(File file : methodsDir.listFiles()) {
			if(file.getName().endsWith(".json")) {
				String fileName = file.getName().replace(".json", "");
				int index = Integer.parseInt(fileName);
				try(FileInputStream fis = new FileInputStream(file)) {
					byte[] bytes = new byte[fis.available()];
					fis.read(bytes);
					InputMethod method = InputMethod.loadJSON(new String(bytes));
					inputMethods.add(index, method);
				} catch(IOException | JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return inputMethods;
	}

	public static void storeMethods(File methodsDir, List<InputMethod> inputMethods) {
		for(int i = 0 ; i < inputMethods.size() ; i++) {
			InputMethod method = inputMethods.get(i);
			File file = new File(methodsDir, i + ".json");
			try(FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(method.toJSON(-1).getBytes());
			} catch(IOException | JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
