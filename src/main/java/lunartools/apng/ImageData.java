package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.apng.chunks.Chunk_IHDR;

/**
 * The imagedata of the 'source-image' used to create the Png object.
 * 
 * @author Thomas Mattel
 */
class ImageData {
	private static Logger logger = LoggerFactory.getLogger(ImageData.class);
	private Png png;
	private Object imagesource;
	private BufferedImage bufferedImage;

	private int[] imageRgbInts;

	/**
	 * Creates an ImageData object, containing the image data of the source-image of the PNG object.
	 * <br>Valid imagesource classes are File and BufferedImage, where-
	 * <br>- a File is any image file than Java can read
	 * <br>- a BufferedImage using either a DataBufferInt or DataBufferByte
	 * 
	 * @param png the png object related to the image source
	 * @param imagesource either a File or BufferedImage object
	 */
	ImageData(Png png,Object imagesource) {
		this.png=png;
		this.imagesource=imagesource;
	}

	/**
	 * Returns the image source, which is either a File or BufferedImage object.
	 * 
	 * @return either a File or BufferedImage object
	 */
	Object getImageSource() {
		return imagesource;
	}

	/**
	 * Returns the BufferedImage of this ImageData.
	 * <br>
	 * @return the BufferedImage of this ImageData
	 */
	BufferedImage getBufferedImage() {
		if(bufferedImage==null) {
			if(imagesource instanceof BufferedImage) {
				bufferedImage=(BufferedImage)imagesource;
			}else if(imagesource instanceof File) {
				bufferedImage=ImageTools.createBufferedImageFromFile((File)imagesource);
			}
		}
		//imageRgbInts=ImageTools.getRgbIntsFromBufferedImage(bufferedImage);
		return bufferedImage;
	}

	/**
	 * Returns pixeldata as bytearray.
	 * 	 * <br>Depending on colourtype, a pixel consists of
	 * <li>greyscale: one byte containing a 8 bit greyscale value
	 * <li>indexed colour: one byte containing the 8 bit colour table index
	 * <li>truecolour: three bytes, red green blue, 8 bit colour value each
	 * 
	 * @return pixeldata as bytearray
	 */
	byte[] getImageBytes() {
		switch(png.getAnimData().getColourType()) {
		case Chunk_IHDR.COLOURTYPE_TRUECOLOUR:
			return ImageTools.getRgbBytesFromBufferedImage(getBufferedImage());
		case Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR:
			int[] pixel=getRgbInts();
			byte[] bytes=new byte[pixel.length];
			int[] hashtable=new int[0x1000000];
			ArrayList<Color> palette=png.getAnimData().getPalette();
			for(int i=0;i<palette.size();i++) {
				hashtable[palette.get(i).getColor()]=i;
			}
			for(int i=0;i<pixel.length;i++) {
				bytes[i]=(byte)hashtable[pixel[i]];
			}
			return bytes;
		case Chunk_IHDR.COLOURTYPE_GREYSCALE:
			return ImageTools.changeIntGreyscaleToByteGreyscale(getRgbInts());
		default:
			throw new RuntimeException("not supported colour type");
		}
	}

	/**
	 * Converts the RGB int array to an palette offset bytearray using the palette of the related animation.
	 * 
	 * @param pixel
	 * @return
	 */
	byte[] convertToPaletteImage(int[] pixel) {
		byte[] bytes=new byte[pixel.length];
		int[] hashtable=new int[0x1000000];
		ArrayList<Color> palette=png.getAnimData().getPalette();
		for(int i=0;i<palette.size();i++) {
			hashtable[palette.get(i).getColor()]=i;
		}
		for(int i=0;i<pixel.length;i++) {
			bytes[i]=(byte)hashtable[pixel[i]];
		}
		return bytes;
	}

	/**
	 * Returns the width of the image source.
	 * 
	 * @return the width of the image source
	 */
	public int getWidth() {
		return getBufferedImage().getWidth();
	}

	/**
	 * Returns the height of the image source.
	 * 
	 * @return the height of the image source
	 */
	public int getHeight() {
		return getBufferedImage().getHeight();
	}

	/**
	 * Returns the image pixel as RGB int array.
	 * 
	 * @return the image pixel as RGB int array
	 */
	int[] getRgbInts() {
		if(imageRgbInts==null) {
			imageRgbInts=ImageTools.getRgbIntsFromBufferedImage(getBufferedImage());
		}
		return imageRgbInts;
	}

	void reset() {
		bufferedImage=null;;
		imageRgbInts=null;
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("ImageData:");
		sb.append("\n\tImageSource: "+imagesource.getClass().getName());
		return sb.toString();
	}

}
