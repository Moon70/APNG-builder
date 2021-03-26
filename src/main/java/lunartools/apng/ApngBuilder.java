package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Builds an APNG (Animated Portable Network Graphic).
 * <br><br><b>This is an alpha version.
 * <br>It does not yet reencode the image data, but only combine existing image data and insert APNG chunks.
 * <br><br>Please note this is a draft, the interface may change.
 * </b>
 * 
 * @author Thomas Mattel
 */
public class ApngBuilder {

	public static Png createPng(BufferedImage bufferedImage, int delay) {
		if(bufferedImage==null) {
			throw new NullPointerException();
		}
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "PNG", baos);
		} catch (IOException e) {
			throw new RuntimeException("error creating png data",e);
		}
		Png png=new Png(baos.toByteArray());
		png.parsePng();
		png.setDelay(delay);
		return png;
	}
	
	public static Png createApng(byte[][] pngsAsBytearray,int delay){
		if(pngsAsBytearray==null) {
			throw new NullPointerException();
		}
		Png apng=new Png(pngsAsBytearray[0]);
		apng.parsePng();
		apng.setDelay(delay);
		for(int i=1;i<pngsAsBytearray.length;i++) {
			Png png=new Png(pngsAsBytearray[i]);
			png.parsePng();
			png.setDelay(delay);
			apng.addPng(png);
		}
		return apng;
	}
	
	/**
	 * Creates a PNG object from a PNG file.
	 * 
	 * @param file The PNG file
	 * @return 
	 * @throws IOException 
	 */
	protected static Png createPng(File file) throws IOException {
		if(file==null) {
			throw new NullPointerException();
		}
		byte[] baPng=FileTools.readFileAsByteArray(file);
		return createPng(baPng);
	}

	/**
	 * Creates a PNG object from a bytearray, which contains the data on a PNG file.
	 * 
	 * @param bytes The bytearray of a PNG file
	 * @return
	 * @throws FileNotFoundException
	 */
	private static Png createPng(byte[] bytes) throws FileNotFoundException {
		if(bytes==null) {
			throw new NullPointerException();
		}
		Png png=new Png(bytes);
		png.parsePng();
		return png;
	}
	
	
	protected static Png createApng(File[] files) throws IOException {
		if(files==null) {
			throw new NullPointerException();
		}
		Png apng=createPng(files[0]);
		apng.parsePng();
		for(int i=1;i<files.length;i++) {
			Png png=createPng(files[i]);
			png.parsePng();
			apng.addPng(png);
		}
		return apng;
	}

}
