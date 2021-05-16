package lunartools.apng;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Calculates a score, to compare which filter type is most likely the best choice for the current scanline.
 * <br>This implementation calculates the score by deflating the previous and the current scanline. The score is the size 
 * of the compresses data.
 * 
 * @author Thomas Mattel
 */
public class FilterScore4{
	protected byte[] currentScanline;
	protected byte[] lastScanline;
	private Deflater deflater;
	
	public FilterScore4() {
		deflater=new Deflater(Deflater.DEFAULT_COMPRESSION);
		deflater.setStrategy(Deflater.HUFFMAN_ONLY);
	}
	
	public void addLine(byte[] baScanline) {
		lastScanline=currentScanline;
		currentScanline=baScanline.clone();
	}

	public int calcScore(byte[] scanline) {
		try {
			deflater.reset();
			ByteArrayOutputStream baosCompressed=new ByteArrayOutputStream();
			DeflaterOutputStream dos=new DeflaterOutputStream(baosCompressed,deflater);
			if(lastScanline!=null) {
				dos.write(lastScanline);
			}
			if(currentScanline!=null) {
				dos.write(currentScanline);
			}
			dos.write(scanline);
			dos.finish();
			dos.flush();
			return baosCompressed.toByteArray().length;
		} catch (Exception e) {
			throw new RuntimeException("error calculating score",e);
		}
	}
}
