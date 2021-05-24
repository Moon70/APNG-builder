package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds an APNG (Animated Portable Network Graphic).
 * <br><br><b>This is an alpha version, the interface may change.
 * </b>
 * 
 * @author Thomas Mattel
 */
public class ApngBuilder {
	private static Logger logger = LoggerFactory.getLogger(ApngBuilder.class);
	private QuantizerAlgorithm quantizerAlgorithm=QuantizerAlgorithm.MEDIAN_CUT;
	private DitheringAlgorithm ditheringAlgorithm=DitheringAlgorithm.SIERRA;
	private boolean flagPngEncoderEnabled=true;
	private boolean flagReencodePngFilesEnabled=true;
	private int minimumNumberOfTransparentPixel=3;
	private int numberOfTruecolourBits=8;
	private int maximumNumberOfColours;
	private int imageDataChunkSize=65536;
	
	public enum QuantizerAlgorithm{
		MEDIAN_CUT
	}
	
	public enum DitheringAlgorithm {
		NO_DITHERING,
		SIMPLE_DITHERING1,
		FLOYD_STEINBERG,
		JARVIS_JUDICE_NINKE,
		STUCKI,
		ATKINSON,
		BURKES,
		SIERRA,
		TWO_ROW_SIERRA,
		SIERRA_LITE
	}

	public ApngBuilder setQuantizerAlgorithm(QuantizerAlgorithm quantizerAlgorithm) {
		this.quantizerAlgorithm=quantizerAlgorithm;
		return this;
	}
	
	QuantizerAlgorithm getQuantizerAlgorithm() {
		return quantizerAlgorithm;
	}
	
	public ApngBuilder setDitheringAlgorithm(DitheringAlgorithm ditheringAlgorithm) {
		this.ditheringAlgorithm=ditheringAlgorithm;
		return this;
	}

	DitheringAlgorithm getDitheringAlgorithm() {
		return ditheringAlgorithm;
	}
	
	/**
	 * To enable/disable the PNG encoder.
	 * <br>enabled (default): APNG-builder uses it´s own PNG encoder, using some APNG features to reduce the filesize.
	 * <br>
	 * <br>disabled: PNGs are made using Java´s ImageIO, multiple PNGs then combined to an APNG without optimizing.
	 * <br>
	 * <br>Hints:
	 * <br>Depending on the images, PNG encoder should produces a smaller files, sometimes drastically.
	 * <br>ImageIO does not optimize (yet), but uses a better compressor, which might result in an even smaller filesize. Furthermore, it´s much faster.
	 * <br>Conclusion:
	 * <br>Set to <code>true</code> if you create an animation of an object that moves in front of a static background.
	 * <br>Set to <code>false</code> if you create an animation from existiung PNG files that are well compressed, or
	 * if you´re previewing an APNG.
	 *  
	 * @param pngEncoder
	 * @return
	 */
	public ApngBuilder enablePngEncoder(boolean pngEncoder) {
		this.flagPngEncoderEnabled=pngEncoder;
		logger.debug("PngEncoderEnabled: {}",this.flagPngEncoderEnabled);
		return this;
	}

	/**
	 * Returns <code>true</code> if PngEncoder is enabled, or <code>false</code> if ImageIO is used to produce the PNG data.
	 * 
	 * @return <code>true</code> if PngEncoder is used to produce the PNG data
	 */
	boolean isPngEncoderEnabled() {
		return flagPngEncoderEnabled;
	}

	/**
	 * enable/disable reencoding of PNG files.
	 * <br>Affects only the builder methods with PNG file input.
	 * <br>
	 * <br>enabled (default): PNG Files will be reencoded, this should (in most cases) produce a smaller filesize.
	 * I.e., the encoder might decide to convert a 24bit truecolor image to a 256 color palette image, if this is possible
	 * in a lossless way.
	 * <br>
	 * <br>disabled: Creates the APNG by taking PNG chunks from existing PNG files, which is very fast. In some cases, when
	 * the animation is hard to optimize and the existing PNG files are well compressed, it could produce a smaller filesize.
	 * 
	 * @param reencodePngFiles
	 * @return
	 */
	public ApngBuilder enableReencodePngFiles(boolean reencodePngFiles) {
		this.flagReencodePngFilesEnabled=reencodePngFiles;
		logger.debug("ReencodePngFilesEnabled: {}",this.flagReencodePngFilesEnabled);
		return this;
	}

	/**
	 * If the input data (the image) is already in PNG format:
	 * <br><code>true</code>: The PngEncoder should reencode the imagedata, to hopefully reduce the filesize.
	 * <br><code>false</code>: The unchanged imagedata ist used to produce the PNG/APNG, which is very fast.
	 * <br>There is no difference in image quality, as long as lossy mode is deactivated, which is default.
	 * 
	 * @return <code>true</code> if PngEncoder is allowed to reencode PNG data.
	 */
	boolean isReencodePngFilesEnabled() {
		return flagReencodePngFilesEnabled;
	}

	/**
	 * The minimum number of pixel in a row that have not changed, before replacing them with transparent pixel.
	 * <br>When <code>0</code>, no pixel gets replaced with transparent pixel.
	 * <br>When <code>1</code>, every unchanged pixel gets replaced with transparent pixel.
	 * <br>When <code>2</code>, unchanged pixel only get replaced if there are at least 2 in a row
	 * <br>and so on...
	 * <br>This is a lossless transformation.
	 * <br>Please note: Depending on the animation, there is also a chance that the data size will increase, specifically if
	 * the animation is a movie sequence.
	 * <br>The encoder should try both ways and take the shorter, this is not implemented yet.
	 * <br>However, if it´s a cartoon-like animation, or an animation in front of a static background, the size gets reduced drastically.
	 * 
	 * @param transparentPixelEnabled
	 * @return
	 */
	public ApngBuilder setMinimumNumberOfTransparentPixel(int minimumNumberOfTransparentPixel) {
		this.minimumNumberOfTransparentPixel=minimumNumberOfTransparentPixel;
		logger.debug("TransparentPixelEnabled: {}",this.minimumNumberOfTransparentPixel);
		return this;
	}

	/** The minimum number of unchanged pixel, before they get replaced with transparent pixel */
	int getMinimumNumberOfTransparentPixel() {
		return minimumNumberOfTransparentPixel;
	}

	/**
	 * The number of bits for each colour component red/green/blue.
	 * <br>Default is 8 for those three components to get 8*3=24 bit truecolour.
	 * <br>Setting to a lower value reduces both image quality and filesize. Depending on the animation, the quality loss is
	 * not or hardly noticeable.
	 * <br>This is no lossless operation.
	 * @param numberOfBits
	 * @return
	 */
	public ApngBuilder setNumberOfTruecolourBits(int numberOfBits) {
		this.numberOfTruecolourBits=numberOfBits;
		logger.debug("TrueColour number of bits: {}",this.numberOfTruecolourBits);
		return this;
	}

	/** @return The number of bits for each colour component red/green/blue */
	int getNumberOfTruecolourBits() {
		return this.numberOfTruecolourBits;
	}

	/**
	 * Sets the maximum colours of the animation.
	 * <br>The total number of used colours (all images of the animation) gets reduced to the given value.
	 * <br>The value <code>0</code> (default) disables this function.
	 * <br>When setting this value to 256, the PNG encoder might decide to convert to a 256 colours palette image.
	 * <br>When setting this value to 255 (or lower), the PNG encoder will convert to a 256 (or lower) colours palette image, using 255 (or lower) colours plus one transparent colour.
	 * 
	 * @param numberOfColours
	 * @return
	 */
	public ApngBuilder setMaximumNumberOfColours(int numberOfColours) {
		this.maximumNumberOfColours=numberOfColours;
		logger.debug("Number of colours for quantizer: {}",this.maximumNumberOfColours);
		return this;
	}

	/** the maximum colours of the animation (all images) */
	int getMaximumNumberOfColours() {
		return this.maximumNumberOfColours;
	}

	/**
	 * Sets the image data chunk size.
	 * <br>The compressed image data gets split in several imagedata chunks.
	 * <br>The default imagedata chunk size is 64k (65536 bytes).
	 * 
	 * @param imageDataChunkSize
	 * @return
	 */
	public ApngBuilder setImageDataChunkSize(int imageDataChunkSize) {
		this.imageDataChunkSize=imageDataChunkSize;
		logger.debug("Image data chunk size: {}",this.imageDataChunkSize);
		return this;
	}
	
	int getImageDataChunkSize() {
		return this.imageDataChunkSize;
	}
	
	/**
	 * Builds a PNG from a given file.
	 * <br>The file can be any image file that Java can read.
	 * <br>This PNG object can be used to add another PNG objects, to create an APNG animation.
	 * 
	 * @param image Any image file that Java can read
	 * @return
	 * @throws IOException
	 */
 	public Png buildPng(File image) throws IOException{
		if(image==null) {
			throw new NullPointerException("image");
		}
		if(!image.exists()) {
			throw new FileNotFoundException(image.getAbsolutePath());
		}
		return new Png(this,image);
	}

	/**
	 * Builds a PNG from a BufferedImage.
	 * <br>This PNG object can be used to add another PNG objects, to create an APNG animation.
	 * 
	 * @param image the BufferedImage to build the Png object
	 * @return the Png object built from the BufferedImage
	 */
	public Png buildPng(BufferedImage image){
		if(image==null) {
			throw new NullPointerException("image");
		}
		return new Png(this,image);
	}

	/**
	 * Builds a PNG from a given file array.
	 * <br>Each file can be any image file that Java can read.
	 * <br>This PNG object can be saved as APNG animation.
	 * <br>The same delay is used for the whole animation.
	 * 
	 * 
	 * @param images Any image files that Java can read
	 * @param delay Delay in milliseconds, the time to wait before the next image of the animation is shown
	 * @return
	 * @throws IOException
	 */
	public Png buildPng(File[] images, int delay) throws IOException{
		if(images==null) {
			throw new NullPointerException();
		}
		Png apng=buildPng(images[0]);
		apng.setDelay(delay);
		for(int i=1;i<images.length;i++) {
			Png png=buildPng(images[i]);
			apng.addPng(png);
			png.setDelay(delay);
		}
		return apng;
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(this.getClass().getSimpleName()+": ");
		sb.append("PngEncoder enabled: "+flagPngEncoderEnabled);
		sb.append(", ReEncode PNG: "+flagReencodePngFilesEnabled);
		sb.append(", TransparentPixel: "+minimumNumberOfTransparentPixel);
		sb.append(", TrueColour bits: "+numberOfTruecolourBits);
		sb.append(", NumberOfColours: "+(maximumNumberOfColours==0?"TrueColour":maximumNumberOfColours));
		return sb.toString();
	}
}
