package me.blog.hgl1002.openwnn.KOKR.softkeyboard;

public class SoftKeyboardPreference {

	protected static final int SPACE_SLIDE_UNIT = 30;
	protected static final int BACKSPACE_SLIDE_UNIT = 250;

	private int keyHeightPortrait = 50, keyHeightLandscape = 42;
	private int longPressTimeout = 300;
	private boolean useFlick;
	private int flickSensitivity = 100, spaceSlideSensitivity = 100;
	private int vibrateDuration = 30;
	private boolean showPreview = false;
	private int keyIcon = 0;

	public int getKeyHeightPortrait() {
		return keyHeightPortrait;
	}

	public void setKeyHeightPortrait(int keyHeightPortrait) {
		this.keyHeightPortrait = keyHeightPortrait;
	}

	public int getKeyHeightLandscape() {
		return keyHeightLandscape;
	}

	public void setKeyHeightLandscape(int keyHeightLandscape) {
		this.keyHeightLandscape = keyHeightLandscape;
	}

	public int getLongPressTimeout() {
		return longPressTimeout;
	}

	public void setLongPressTimeout(int longPressTimeout) {
		this.longPressTimeout = longPressTimeout;
	}

	public boolean isUseFlick() {
		return useFlick;
	}

	public void setUseFlick(boolean useFlick) {
		this.useFlick = useFlick;
	}

	public int getFlickSensitivity() {
		return flickSensitivity;
	}

	public void setFlickSensitivity(int flickSensitivity) {
		this.flickSensitivity = flickSensitivity;
	}

	public int getSpaceSlideSensitivity() {
		return spaceSlideSensitivity;
	}

	public void setSpaceSlideSensitivity(int spaceSlideSensitivity) {
		this.spaceSlideSensitivity = spaceSlideSensitivity;
	}

	public int getVibrateDuration() {
		return vibrateDuration;
	}

	public void setVibrateDuration(int vibrateDuration) {
		this.vibrateDuration = vibrateDuration;
	}

	public boolean isShowPreview() {
		return showPreview;
	}

	public void setShowPreview(boolean showPreview) {
		this.showPreview = showPreview;
	}

	public int getKeyIcon() {
		return keyIcon;
	}

	public void setKeyIcon(int keyIcon) {
		this.keyIcon = keyIcon;
	}
}
