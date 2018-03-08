package io.github.lee0701.heonot.inputmethod.modules.global;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.github.lee0701.heonot.HeonOt;
import io.github.lee0701.heonot.inputmethod.event.CommitCharEvent;
import io.github.lee0701.heonot.inputmethod.event.HardKeyEvent;
import io.github.lee0701.heonot.inputmethod.modules.InputMethodModule;
import io.github.lee0701.heonot.inputmethod.modules.hardkeyboard.KeyStroke;
import io.github.lee0701.heonot.inputmethod.scripting.TreeEvaluator;
import io.github.lee0701.heonot.inputmethod.scripting.nodes.TreeNode;

public class ShortcutProcessor extends InputMethodModule {

	private List<Shortcut> shortcuts = new ArrayList<>();

	@Override
	public void init() {

	}

	@Override
	public void pause() {

	}

	@Subscribe(priority = 1)
	public void onHardKey(HardKeyEvent event) {
		if(event.getAction() == HardKeyEvent.HardKeyAction.PRESS
				&& processShortcut(event.getKeyCode(), event.isAltPressed(), event.isShiftPressed())) {
			EventBus.getDefault().cancelEventDelivery(event);
		}
	}

	public boolean processShortcut(int keyCode, boolean altPressed, boolean shiftPressed) {
		TreeEvaluator evaluator = HeonOt.getInstance().getTreeEvaluator();
		for(Shortcut shortcut : shortcuts) {
			KeyStroke keyStroke = shortcut.getKeyStroke();
			if(keyStroke.getKeyCode() == keyCode
					&& keyStroke.isAlt() == altPressed
					&& keyStroke.isShift() == shiftPressed) {
				evaluator.setVariables(HeonOt.getInstance().getVariables());
				long result = evaluator.eval(shortcut.getTreeNode());
				switch(shortcut.getMode()) {
				case Shortcut.MODE_CHANGE:
					HeonOt.getInstance().changeInputMethod((int) result);
					break;

				case Shortcut.MODE_INPUT:
					EventBus.getDefault().post(new CommitCharEvent((char) result, 1));
					break;

				}
				return true;
			}
		}
		return false;
	}

	@Override
	public Object clone() {
		return null;
	}

	public List<Shortcut> getShortcuts() {
		return shortcuts;
	}

	public void setShortcuts(List<Shortcut> shortcuts) {
		this.shortcuts = shortcuts;
	}

	public static class Shortcut {

		public static final int MODE_NONE = 0;
		public static final int MODE_CHANGE = 1;
		public static final int MODE_INPUT = 2;

		private KeyStroke keyStroke;
		private int mode;
		private TreeNode treeNode;

		public Shortcut(KeyStroke keyStroke, int mode, TreeNode treeNode) {
			this.keyStroke = keyStroke;
			this.mode = mode;
			this.treeNode = treeNode;
		}

		public KeyStroke getKeyStroke() {
			return keyStroke;
		}

		public void setKeyStroke(KeyStroke keyStroke) {
			this.keyStroke = keyStroke;
		}

		public int getMode() {
			return mode;
		}

		public void setMode(int mode) {
			this.mode = mode;
		}

		public TreeNode getTreeNode() {
			return treeNode;
		}

		public void setTreeNode(TreeNode treeNode) {
			this.treeNode = treeNode;
		}
	}

}
