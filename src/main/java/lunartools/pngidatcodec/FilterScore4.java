package lunartools.pngidatcodec;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Calculates a score, to compare which filter type is most likely the best choice for the current scanline.
 * <br>This implementation calculates the score by deflating the two previous and the current scanline. The score is the size 
 * of the compresses data.
 * 
 * @author Thomas Mattel
 */
public class FilterScore4 extends FilterScore{

	@Override
	public int calcScore(byte[] scanline) {
		try {
			Deflater deflater=new Deflater(Deflater.DEFAULT_COMPRESSION);
			deflater.setStrategy(Deflater.HUFFMAN_ONLY);
			ByteArrayOutputStream baosCompressed=new ByteArrayOutputStream();
			DeflaterOutputStream dos=new DeflaterOutputStream(baosCompressed);
			if(secondLastScanline!=null) {
				dos.write(secondLastScanline);
			}
			if(lastScanline!=null) {
				dos.write(lastScanline);
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
