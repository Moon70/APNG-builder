package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.pngidatcodec.ImageTools;

public class ImageData {
	private static Logger logger = LoggerFactory.getLogger(ImageData.class);
	private Png png;
	private Object imagesource;
	private BufferedImage bufferedImage;

	private int[] imageRgbInts;

	ImageData(Png png,Object imagesource) {
		this.png=png;
		this.imagesource=imagesource;
	}

	Object getImageSource() {
		return imagesource;
	}

	BufferedImage getBufferedImage() {
		if(bufferedImage==null) {
			if(imagesource instanceof BufferedImage) {
				bufferedImage=(BufferedImage)imagesource;
			}else if(imagesource instanceof File) {
				bufferedImage=ImageTools.createBufferedImageFromFile((File)imagesource);
			}
		}
		imageRgbInts=ImageTools.getRgbIntsFromBufferedImage(bufferedImage);
		return bufferedImage;
	}

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

	public int getWidth() {
		return getBufferedImage().getWidth();
	}

	public int getHeight() {
		return getBufferedImage().getHeight();
	}

	int[] getRgbInts() {
		if(imageRgbInts==null) {
			imageRgbInts=ImageTools.getRgbIntsFromBufferedImage(getBufferedImage());
		}
		return imageRgbInts;
	}

	private void applyBitmasc(int[] intImage,int numberOfBits) {
		int masc=0;
		for(int i=0;i<8;i++) {
			masc=masc<<1;
			if(i<numberOfBits) {
				masc+=0b000000010000000100000001;
			}
		}
		for(int i=0;i<intImage.length;i++) {
			intImage[i]=intImage[i]&masc;
		}
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("ImageData:");
		sb.append("\n\tImageSource: "+imagesource.getClass().getName());
		return sb.toString();
	}

}
