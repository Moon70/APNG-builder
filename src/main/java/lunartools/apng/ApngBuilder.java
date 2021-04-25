package lunartools.apng;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds an APNG (Animated Portable Network Graphic).
 * <br><br><b>This is an alpha version.
 * <br>Please note this is a draft, the interface may change.
 * </b>
 * 
 * @author Thomas Mattel
 */
public class ApngBuilder {
	private static Logger logger = LoggerFactory.getLogger(ApngBuilder.class);
	private boolean flagPngEncoderEnabled=true;
	private boolean flagReencodePngFilesEnabled=true;
	private int numberOfTruecolourBits=8;

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

	public ApngBuilder setNumberOfTruecolourBits(int numberOfBits) {
		this.numberOfTruecolourBits=numberOfBits;
		logger.debug("TrueColour number of bits: {}",this.numberOfTruecolourBits);
		return this;
	}

	int getNumberOfTruecolourBits() {
		return this.numberOfTruecolourBits;
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

}
