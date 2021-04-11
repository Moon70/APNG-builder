package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import lunartools.apng.chunks.Chunk_IDAT;
import lunartools.apng.chunks.Chunk_IEND;
import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.pngidatcodec.ImageTools;
import lunartools.pngidatcodec.ImagedataOptimizer;
import lunartools.pngidatcodec.PngEncoder;

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

	/**
	 * @deprecated temporary only for test/development
	 */
	public static Png test(File fileImage, boolean usePngEncoder) throws Exception{
		if(fileImage==null) {
			throw new NullPointerException();
		}
		if(usePngEncoder) {
			return createPngViaOwnEncoder(ImageTools.createBufferedImageFromFile(fileImage),null);
		}else {
			byte[] baPng=FileTools.readFileAsByteArray(fileImage);
			Png png=new Png(baPng);
			png.parsePng();
			return png;
		}
	}

	/**
	 * Creates an APNG from the given image files and delay.
	 * 
	 * @param files Array of files that Java can read.
	 * @param delay Delay in milliseconds before to switch to the next image.
	 * @param usePngEncoder
	 * @return
	 * @throws IOException
	 */
	public static Png createApng(File[] files, int delay, boolean usePngEncoder) throws IOException {
		if(files==null) {
			throw new NullPointerException();
		}
		BufferedImage bufferedImage=ImageTools.createBufferedImageFromFile(files[0]);
		Png apng=createPng(bufferedImage,null,delay,usePngEncoder);
		//FileTools.writeFile(new File("c:/temp/ApngSingle0.png"), apng.toByteArray(), false);
		BufferedImage bufferedImageReference;
		for(int i=1;i<files.length;i++) {
			bufferedImageReference=bufferedImage;
			bufferedImage=ImageTools.createBufferedImageFromFile(files[i]);
			Png png=createPng(bufferedImage,bufferedImageReference,delay,usePngEncoder);
			apng.addPng(png);
			//FileTools.writeFile(new File("c:/temp/ApngSingle"+i+".png"), png.toByteArray(), false);
		}
		return apng;
	}

	public static Png createPng(BufferedImage bufferedImage,BufferedImage bufferedImageReference, int delay, boolean usePngEncoder) {
		if(bufferedImage==null) {
			throw new NullPointerException();
		}
		Png png=null;
		if(usePngEncoder) {
			png=createPngViaOwnEncoder(bufferedImage,bufferedImageReference);
		}else {
			byte[] baJavaPng=createPngViaJava(bufferedImage);
			png=new Png(baJavaPng);
			png.parsePng();
		}
		png.setDelay(delay);
		return png;
	}

	private static byte[] createPngViaJava(BufferedImage bufferedImage) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "PNG", baos);
		} catch (IOException e) {
			throw new RuntimeException("error creating PNG data",e);
		}
		return baos.toByteArray();
	}

	private static Png createPngViaOwnEncoder(BufferedImage bufferedImage,BufferedImage bufferedImageReference) {
		try {
			int width=bufferedImage.getWidth();
			int height=bufferedImage.getHeight();
			//			System.out.println("Dimension: "+width+" x "+height);
			//			System.out.println("ColorModel: "+bufferedImage.getColorModel());
			//			System.out.println("Class: "+bufferedImage.getClass().getName());
			int bitdepth=8;
			int colourtype=2;
			byte[] baImageRaw;
			Chunk_IHDR chunk_IHDR;
			Png png=new Png();
			if(bufferedImageReference==null) {
				PngEncoder pngEncoder=new PngEncoder();
				baImageRaw=pngEncoder.encodePng(bufferedImage);
				chunk_IHDR=new Chunk_IHDR(width, height, bitdepth, colourtype);
			}else {
				ImagedataOptimizer imagedataOptimizer=new ImagedataOptimizer();
				imagedataOptimizer.createOptimizedImage(bufferedImage, bufferedImageReference);
				//System.out.println("imagedataOptimizer: "+imagedataOptimizer);
				baImageRaw=new PngEncoder().encodePng(imagedataOptimizer.getImagedata(),imagedataOptimizer.getwidth(),imagedataOptimizer.getHeight());
				chunk_IHDR=new Chunk_IHDR(imagedataOptimizer.getwidth(), imagedataOptimizer.getHeight(), bitdepth, colourtype);
				png.setOffset(imagedataOptimizer.getOffsetX(),imagedataOptimizer.getOffsetY());
			}
			png.addChunk(chunk_IHDR);

			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			Deflater deflater=new Deflater(Deflater.BEST_COMPRESSION);
			DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(baos,deflater);
			deflaterOutputStream.write(baImageRaw);
			deflaterOutputStream.finish();
			deflaterOutputStream.flush();
			deflaterOutputStream.close();
			byte[] imagedataCompressed=baos.toByteArray();

			final int chunkdatasize=65536;
			for(int i=0;i<imagedataCompressed.length;i+=chunkdatasize) {
				int len=i+chunkdatasize;
				if(len>imagedataCompressed.length) {
					len=imagedataCompressed.length;
				}
				Chunk_IDAT chunk_IDAT=new Chunk_IDAT(Arrays.copyOfRange(imagedataCompressed, i, len));
				png.addChunk(chunk_IDAT);
			}
			Chunk_IEND chunk_IEND=new Chunk_IEND();
			png.addChunk(chunk_IEND);
			return png;
		} catch (Exception e) {
			throw new RuntimeException("error encoding PNG",e);
		}
	}

}
