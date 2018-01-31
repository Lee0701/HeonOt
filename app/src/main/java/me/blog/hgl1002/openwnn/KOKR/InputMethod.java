package me.blog.hgl1002.openwnn.KOKR;

import me.blog.hgl1002.openwnn.KOKR.generator.CharacterGenerator;
import me.blog.hgl1002.openwnn.KOKR.hardkeyboard.HardKeyboard;
import me.blog.hgl1002.openwnn.KOKR.softkeyboard.SoftKeyboard;

public class InputMethod {

	SoftKeyboard softKeyboard;
	HardKeyboard hardKeyboard;
	CharacterGenerator characterGenerator;

	public InputMethod(SoftKeyboard softKeyboard, HardKeyboard hardKeyboard, CharacterGenerator characterGenerator) {
		this.softKeyboard = softKeyboard;
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = characterGenerator;
	}

	public void init() {
		softKeyboard.init();
		hardKeyboard.init();
		characterGenerator.init();
	}

	public SoftKeyboard getSoftKeyboard() {
		return softKeyboard;
	}

	public void setSoftKeyboard(SoftKeyboard softKeyboard) {
		this.softKeyboard = softKeyboard;
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
