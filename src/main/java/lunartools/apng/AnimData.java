package lunartools.apng;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.apng.chunks.Chunk_IHDR;

public class AnimData {
	private static Logger logger = LoggerFactory.getLogger(AnimData.class);
	private Png png;
	private int numberOfColours;
	private Color unusedColour;
	private int colourtype;
	private int bytesPerPixel;
	private boolean flagIsGreyscale;
	private ArrayList<Color> palette;

	AnimData(Png png) {
		this.png=png;
	}

	int getNumberOfColours() {
		if(numberOfColours==0) {
			analyzeImages();
		}
		return numberOfColours;
	}

	boolean isGreyscale() {
		if(numberOfColours==0) {
			analyzeImages();
		}
		return flagIsGreyscale;
	}

	ArrayList<Color> getPalette(){
		if(numberOfColours==0) {
			analyzeImages();
		}
		return palette;
	}

	public Color findUnusedColour() {
		if(unusedColour==null) {
			analyzeImages();
		}
		return unusedColour;
	}

	int getBytesPerPixel() {
		if(bytesPerPixel==0) {
			analyzeImages();
		}
		return bytesPerPixel;
	}

	int getColourType() {
		if(bytesPerPixel==0) {
			analyzeImages();
		}
		return colourtype;
	}

	private void analyzeImages() {
		analyzeColours();

		int numberOfBits=png.getFirstPng().getBuilder().getNumberOfTruecolourBits();
		if(numberOfBits<8) {
			if(numberOfColours<256) {
				logger.debug("ignoring bitmasc, number of colours < 256: {}",numberOfColours);
				return;
			}else {
				logger.debug("converting colour components to {} bit",numberOfBits);
				ArrayList<ImageData> allImagedata=png.findAllImagedata();
				for(int i=0;i<allImagedata.size();i++) {
					ImageData imageData=allImagedata.get(i);
					int[] imageRgbInts=imageData.getRgbInts();
					applyBitmasc(imageRgbInts,numberOfBits);
				}
				analyzeColours();
			}
		}

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
				logger.debug("unused grey colour found: {}",Integer.toHexString(pixel));
				break;
			}
		}
		if(unusedColour==null) {
			for(int i=0;i<colourCount.length;i++) {
				if(colourCount[i]==0) {
					unusedColour=new Color(i);
					logger.debug("unused RGB colour found: {}",Integer.toHexString(i));
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
			logger.debug("colour type is truecolour");
		}else{
			this.palette=palette;
			this.flagIsGreyscale=isGreyscale;
			bytesPerPixel=1;
			if(isGreyscale) {
				colourtype=Chunk_IHDR.COLOURTYPE_GREYSCALE;
				logger.debug("colour type is hreyscale");
			}else {
				colourtype=Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR;
				logger.debug("colour type is indexed colour");
			}
		}
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
		sb.append("AnimData:");
		sb.append("\n\tnumberOfColours: "+getNumberOfColours());
		sb.append("\n\tflagIsGreyscale: "+flagIsGreyscale);
		sb.append("\n\tpalette: "+palette);
		return sb.toString();
	}

}