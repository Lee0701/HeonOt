package me.blog.hgl1002.openwnn.KOKR;

import me.blog.hgl1002.openwnn.KOKR.generator.CharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.HardKeyboard;

public class InputMethod {

	HardKeyboard hardKeyboard;
	CharacterGenerator characterGenerator;

	public InputMethod(HardKeyboard hardKeyboard, CharacterGenerator characterGenerator) {
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = characterGenerator;
	}

	public void init() {
		hardKeyboard.init();
		characterGenerator.init();
	}

	public HardKeyboard getHardKeyboard() {
		return hardKeyboard;
	}

	public void setHardKeyboard(HardKeyboard hardKeyboard) {
		this.hardKeyboard = hardKeyboard;
	}

	public CharacterGenerator getCharacterGenerator() {
		return characterGenerator;
	}

	public void setCharacterGenerator(CharacterGenerator characterGenerator) {
		this.characterGenerator = characterGenerator;
	}
}
