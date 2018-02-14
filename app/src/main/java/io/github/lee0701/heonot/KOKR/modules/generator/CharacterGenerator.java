package io.github.lee0701.heonot.KOKR.modules.generator;

import io.github.lee0701.heonot.KOKR.modules.InputMethodModule;

public abstract class CharacterGenerator extends InputMethodModule{

	public abstract void input(long code);

	public abstract void backspace(int mode);

}
