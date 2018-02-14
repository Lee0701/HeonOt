package io.github.lee0701.heonot.KOKR;

import io.github.lee0701.heonot.KOKR.softkeyboard.SoftKeyboard;
import io.github.lee0701.heonot.KOKR.generator.CharacterGenerator;
import io.github.lee0701.heonot.KOKR.hardkeyboard.HardKeyboard;
import io.github.lee0701.heonot.HeonOt;

public class InputMethod {

	SoftKeyboard softKeyboard;
	HardKeyboard hardKeyboard;
	CharacterGenerator characterGenerator;

	public InputMethod(HeonOt parent, SoftKeyboard softKeyboard, HardKeyboard hardKeyboard, CharacterGenerator characterGenerator) {
		this.softKeyboard = softKeyboard;
		this.hardKeyboard = hardKeyboard;
		this.characterGenerator = characterGenerator;

		softKeyboard.addListener(parent);
		hardKeyboard.addListener(parent);
		characterGenerator.addListener(parent);

		softKeyboard.addListener(hardKeyboard);
		hardKeyboard.addListener(softKeyboard);
		hardKeyboard.addListener(characterGenerator);
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
