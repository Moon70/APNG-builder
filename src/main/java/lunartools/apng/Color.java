package lunartools.apng;

public class Color{
	private int color;
	private int count;
	
	private int red;
	private int green;
	private int blue;

	Color(int color) {
		this.color=color&0xffffff;
		this.red=(color>>16)&0xff;
		this.green=(color>>8) & 0xff;
		this.blue=color & 0xff;
	}
	
	Color(int color, int count) {
		this(color);
		this.count=count;
	}

	boolean isGrey() {
		return red==green && red==blue;
	}
	
	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public int getColor() {
		return color;
	}
	
	public String toString() {
		return Integer.toHexString(color);
	}
	
}
