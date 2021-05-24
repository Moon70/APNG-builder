package lunartools.apng;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.apng.chunks.Chunk_IHDR.ColourType;
import lunartools.colorquantizer.DitheringAlgorithm;
import lunartools.colorquantizer.GPAC;
import lunartools.colorquantizer.QuantizerAlgorithm;

/**
 * The common data of all images of this animation (APNG).
 * 
 * @author Thomas Mattel
 */
class AnimData {
	private static Logger logger = LoggerFactory.getLogger(AnimData.class);
	private Png png;
	private int numberOfColours;
	private ColourRGB unusedColour;
	private ColourType colourtype;
	private int bytesPerPixel;
	private boolean flagIsGreyscale;
	private ArrayList<ColourRGB> palette;

	AnimData(Png png) {
		this.png=png;
	}

	/**
	 * The total number of used colours of all images of this animation.
	 * 
	 * @return total number of used colours of all images of this animation
	 */
	int getNumberOfColours() {
		if(numberOfColours==0) {
			analyzeImages();
		}
		return numberOfColours;
	}

	/**
	 * Returns <code>true</code> if all images of this animation are greyscale.
	 * 
	 * @return <code>true</code> if all images of this animation are greyscale
	 */
	boolean isGreyscale() {
		if(numberOfColours==0) {
			analyzeImages();
		}
		return flagIsGreyscale;
	}

	/**
	 * Returns the common colour palette of all images of this animation.
	 * <br>If the colourtype is not indexed colour, then <code>null</code> is returned
	 * 
	 * @return the common colour palette of all images of this animation, or null
	 */
	ArrayList<ColourRGB> getPalette(){
		if(numberOfColours==0) {
			analyzeImages();
		}
		return palette;
	}

	/**
	 * An unused colour, or <code>null</code> if there is no unused colour.
	 * 
	 * @return An unused colour, or <code>null</code> if there is no unused colour
	 */
	ColourRGB getUnusedColour() {
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

	ColourType getColourType() {
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

		int maximumNumberOfColours=png.getFirstPng().getBuilder().getMaximumNumberOfColours();
		if(maximumNumberOfColours!=0) {
			ArrayList<int[]> arraylistImagesPixelInts = new ArrayList<int[]>();
			ArrayList<ImageData> allImagedata=png.findAllImagedata();
//			int[] colourCount=new int[0x1000000];
			int width=0;
			int height=0;
			for(int k=0;k<allImagedata.size();k++) {
				ImageData imageData=allImagedata.get(k);
				int[] imageRgbInts=imageData.getRgbInts();
				width=imageData.getWidth();
				height=imageData.getHeight();
//				for(int i=0;i<imageRgbInts.length;i++) {
//					colourCount[imageRgbInts[i]]++;
//				}
				arraylistImagesPixelInts.add(imageRgbInts);
			}
			logger.debug("calling colour quantizer");
			quantizeColours(arraylistImagesPixelInts,width,height,maximumNumberOfColours);
//			for(int k=0;k<allImagedata.size();k++) {
//				ImageData imageData=allImagedata.get(k);
//				int[] imageRgbInts=imageData.getRgbInts();
//				for(int i=0;i<imageRgbInts.length;i++) {
//					imageRgbInts[i]=colourCount[imageRgbInts[i]];
//				}
//			}
			analyzeColours();
			return;
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
				unusedColour=new ColourRGB(pixel);
				logger.debug("unused grey colour found: {}",Integer.toHexString(pixel));
				break;
			}
		}
		if(unusedColour==null) {
			for(int i=0;i<colourCount.length;i++) {
				if(colourCount[i]==0) {
					unusedColour=new ColourRGB(i);
					logger.debug("unused RGB colour found: {}",Integer.toHexString(i));
					break;
				}
			}
		}

		int count=0;
		ArrayList<ColourRGB> palette=new ArrayList<ColourRGB>();
		palette.add(new ColourRGB(0));
		boolean isGreyscale=true;
		for(int i=0;i<colourCount.length;i++) {
			if(colourCount[i]>0) {
				if(palette.size()<256) {
					ColourRGB color=new ColourRGB(i);
					palette.add(color);
					isGreyscale=isGreyscale&color.isGrey();
				}
				count++;
			}
		}
		numberOfColours=count;
		logger.debug("number of colours: {}",numberOfColours);
		if(count>256) {
			colourtype=ColourType.TRUECOLOUR;
			bytesPerPixel=3;
			logger.debug("colour type is truecolour");
		}else{
			this.flagIsGreyscale=isGreyscale;
			bytesPerPixel=1;
			if(isGreyscale) {
				colourtype=ColourType.GREYSCALE;
				logger.debug("colour type is greyscale");
			}else {
				this.palette=palette;
				colourtype=ColourType.INDEXEDCOLOUR;
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
			intImage[i]&=masc;
		}
	}

	private void quantizeColours(ArrayList<int[]> arraylistImagesPixelInts,int width,int height,int numberOfColours) {
		GPAC gpac=new GPAC();
		ApngBuilder builder=png.getFirstPng().getBuilder();
		
		switch(builder.getQuantizerAlgorithm()) {
		case MEDIAN_CUT:
			gpac.setQuantizerAlgorithm(QuantizerAlgorithm.MEDIAN_CUT);
			break;
		default:
			throw new RuntimeException("colour quantizer algorithm not supported: "+builder.getQuantizerAlgorithm());
		}
		
		switch(builder.getDitheringAlgorithm()) {
		case NO_DITHERING:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.NO_DITHERING);
			break;
		case SIMPLE_DITHERING1:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.SIMPLE_DITHERING1);
			break;
		case FLOYD_STEINBERG:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.FLOYD_STEINBERG);
			break;
		case JARVIS_JUDICE_NINKE:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.JARVIS_JUDICE_NINKE);
			break;
		case STUCKI:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.STUCKI);
			break;
		case ATKINSON:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.ATKINSON);
			break;
		case BURKES:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.BURKES);
			break;
		case SIERRA:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.SIERRA);
			break;
		case TWO_ROW_SIERRA:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.TWO_ROW_SIERRA);
			break;
		case SIERRA_LITE:
			gpac.setDitheringAlgorithm(DitheringAlgorithm.SIERRA_LITE);
			break;
		default:
			throw new RuntimeException("dithering algorithm not supported: "+builder.getDitheringAlgorithm());
		}
		gpac.quantizeColours(arraylistImagesPixelInts,width,height,numberOfColours);
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("AnimData:");
		sb.append("\n\tnumberOfColours: "+getNumberOfColours());
		sb.append("\n\tflagIsGreyscale: "+flagIsGreyscale);
		sb.append("\n\tpalette: "+palette);
		return sb.toString();
	}
	
}
