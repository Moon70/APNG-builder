package lunartools.apng;

public class ColourRGB{
	private int colour;
	private int red;
	private int green;
	private int blue;

	ColourRGB(int colour) {
		colour=this.colour=colour&0xffffff;
		this.red=(colour>>16);
		this.green=(colour>>8) & 0xff;
		this.blue=colour & 0xff;
	}

	public int getColour() {
		return colour;
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
	
	boolean isGrey() {
		return red==green && red==blue;
	}
	
	public String toString() {
		return Integer.toHexString(colour);
	}
	
}
