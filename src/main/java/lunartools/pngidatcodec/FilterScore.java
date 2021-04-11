package lunartools.pngidatcodec;

/**
 * This class is temporary used as a baseclass for various FilterScore classes, to help to compare different
 * strategies to find the most effective filter type for a scanline.
 * 
 * @author Thomas Mattel
 */
public abstract class FilterScore {
	protected int[] globalHash=new int[256];
	protected int[] lastHash;

	protected byte[] lastScanline;
	protected byte[] secondLastScanline;

	public abstract int calcScore(byte[] baLine);

	public void addLine(byte[] ba) {
		lastHash=new int[256];
		secondLastScanline=lastScanline;
		lastScanline=ba;
		for(int i=0;i<ba.length;i++) {
			int b=ba[i];
			if(b<0) {
				b+=256;
			}
			lastHash[b]++;
			globalHash[b]++;
		}
	}

}
