package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.pngidatcodec.ImageTools;

public class ImageData {
	private Png png;
	private Object imagesource;
	private BufferedImage bufferedImage;
	private Color unusedColour;

	private int numberOfColours;
	private boolean flagIsGreyscale;
	private ArrayList<Color> palette;
	private int colourtype;
	private int bytesPerPixel;

	private int colourCountCode;
	private int[] imageRgbInts;

	ImageData(Png png,Object imagesource) {
		this.png=png;
		this.imagesource=imagesource;
	}

	Object getImageSource() {
		return imagesource;
	}

	BufferedImage getBufferedImage() {
		if(imagesource instanceof BufferedImage) {
			return (BufferedImage)imagesource;
		}
		if(bufferedImage==null && imagesource instanceof File) {
			bufferedImage=ImageTools.createBufferedImageFromFile((File)imagesource);
		}
		return bufferedImage;
	}

	int getNumberOfColours() {
		if(numberOfColours==0) {
			analyzeColours();
		}
		return numberOfColours;
	}

	boolean isGreyscale() {
		if(numberOfColours==0) {
			analyzeColours();
		}
		return flagIsGreyscale;
	}

	int getBytesPerPixel() {
		if(bytesPerPixel==0) {
			analyzeColours();
		}
		return bytesPerPixel;
	}

	byte[] getImageBytes() {
		switch(colourtype) {
		case Chunk_IHDR.COLOURTYPE_TRUECOLOUR:
			return ImageTools.getRgbBytesFromBufferedImage(getBufferedImage());
		case Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR:
			int[] pixel=getRgbInts();
			byte[] bytes=new byte[pixel.length];
			int[] hashtable=new int[0x1000000];
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
		for(int i=0;i<palette.size();i++) {
			hashtable[palette.get(i).getColor()]=i;
		}
		for(int i=0;i<pixel.length;i++) {
			bytes[i]=(byte)hashtable[pixel[i]];
		}
		return bytes;
	}

	ArrayList<Color> getPalette(){
		if(numberOfColours==0) {
			analyzeColours();
		}
		return palette;
	}

	private void analyzeColours() {
		ArrayList<ImageData> allImagedata=png.findAllImagedata();
		int[] colourCount=new int[0x1000000];
		for(int k=0;k<allImagedata.size();k++) {
			ImageData imageData=allImagedata.get(k);
			int[] imageRgbInts=imageData.getRgbInts();
			for(int i=0;i<imageRgbInts.length;i++) {
				colourCount[imageRgbInts[i]]++;
			}
		}

		//first look if there is an unused 'grey'colour, to make the compressor happy
		for(int i=0;i<256;i++) {
			int pixel=(i<<16)|(i<<8)|i;
			if(colourCount[pixel]==0) {
				unusedColour=new Color(pixel);
				break;
			}
		}
		if(unusedColour==null) {
			for(int i=0;i<colourCount.length;i++) {
				if(colourCount[i]==0) {
					unusedColour=new Color(i);
					break;
				}
			}
		}

		int count=0;
		ArrayList<Color> palette=new ArrayList<Color>();
		palette.add(new Color(0));
		boolean isGreyscale=true;
		for(int i=0;i<colourCount.length;i++) {
			if(colourCount[i]>0) {
				if(palette.size()<256) {
					Color color=new Color(i,colourCount[i]);
					palette.add(color);
					isGreyscale=isGreyscale&color.isGrey();
				}
				count++;
			}
		}
		numberOfColours=count;
		if(count>256) {
			colourtype=Chunk_IHDR.COLOURTYPE_TRUECOLOUR;
			bytesPerPixel=3;
		}else{
			this.palette=palette;
			this.flagIsGreyscale=isGreyscale;
			bytesPerPixel=1;
			if(isGreyscale) {
				colourtype=Chunk_IHDR.COLOURTYPE_GREYSCALE;
			}else {
				colourtype=Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR;
			}
		}

	}

	public Color findUnusedColour() {
		if(unusedColour==null) {
			analyzeColours();
		}
		return unusedColour;
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

	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("ImageData:");
		sb.append("\n\tImageSource: "+imagesource.getClass().getName());
		sb.append("\n\tcolourCountCode: "+colourCountCode);
		sb.append("\n\tnumberOfColours: "+getNumberOfColours());
		sb.append("\n\tflagIsGreyscale: "+flagIsGreyscale);
		sb.append("\n\tpalette: "+palette);
		return sb.toString();
	}

}
