package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.ByteTools;
import lunartools.FileTools;
import lunartools.apng.chunks.Chunk;
import lunartools.apng.chunks.ChunkFactory;
import lunartools.apng.chunks.Chunk_IDAT;
import lunartools.apng.chunks.Chunk_IEND;
import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.apng.chunks.Chunk_acTL;
import lunartools.apng.chunks.Chunk_fcTL;
import lunartools.apng.chunks.Chunk_fcTL.ApngBlendOperation;
import lunartools.apng.chunks.Chunk_fcTL.ApngDisposeOperation;
import lunartools.apng.chunks.Chunk_fdAT;
import lunartools.apng.chunks.Chunk_tEXt;
import lunartools.apng.chunks.Chunk_zTXt;

/**
 * Represents a PNG (Portable Network Graphics)
 * <br>A Png instance holds the binary data of a PNG file.
 * <br>More Png instances can be added to an instance, to create an animated PNG file.
 * <br><br><b>This is an alpha version.
 * </b>
 * 
 * @author Thomas Mattel
 */
public class Png {
	private static Logger logger = LoggerFactory.getLogger(Png.class);
	private static final byte[] PNG_SIGNATURE=new byte[] {(byte)0x89,(byte)0x50,(byte)0x4e,(byte)0x47,(byte)0x0d,(byte)0x0a,(byte)0x1a,(byte)0x0a};
	private ApngBuilder builder;
	private ImageData imageData;
	private Png firstPng=this;
	private Png previousPng;
	private AnimData animData=new AnimData(this);

	private boolean flagImageDataProcessed;

	byte[] baPng;
	private ArrayList<Chunk> chunklist;
	private ArrayList<Png> listPng=new ArrayList<Png>();
	private Chunk_IHDR chunk_IHDR;
	private int delay;
	private int offsetX;
	private int offsetY;

	/**
	 * Creates a Png object from the given image file.
	 * <br>Image file format: Anything that Java can read.
	 * 
	 * @param builder the calling ApngBuilder object
	 * @param fileImage the File to build the Png object
	 */
	Png(ApngBuilder builder,File fileImage){
		this.builder=builder;
		this.imageData=new ImageData(this,fileImage);
	}

	/**
	 * Creates a Png object from the given BufferedImage.
	 * 
	 * @param builder the calling ApngBuilder object
	 * @param image the BufferedImage to build the Png object
	 */
	Png(ApngBuilder builder,BufferedImage bufferedImage){
		this.builder=builder;
		this.imageData=new ImageData(this,bufferedImage);
	}

	ImageData getImageData() {
		return imageData;
	}

	AnimData getAnimData() {
		if(firstPng==this) {
			return animData;
		}else {
			return firstPng.getAnimData();
		}
	}

	/**
	 * Returns the concatenated imagedata of all IDAT chunks of this PNG.
	 * 
	 * @return the compressed imagedata of this PNG.
	 */
	byte[] getCompressedImagedataBytes() {
		processImageData();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			for(int i=0;i<chunklist.size();i++) {
				if(chunklist.get(i) instanceof Chunk_IDAT) {
					Chunk_IDAT chunk=(Chunk_IDAT)chunklist.get(i);
					baos.write(chunk.getChunkData());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading imagedata",e);
		}
		return baos.toByteArray();
	}

	/**
	 * Returns the decompressed concatenated imagedata of all IDAT chunks of this PNG.
	 * 
	 * @return the decompressed imagedata of this PNG.
	 */
	byte[] getDecompressedImagedataBytes() {
		processImageData();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			Inflater inflater=new Inflater();
			InflaterOutputStream inflaterOutputStream=new InflaterOutputStream(baos,inflater);
			inflaterOutputStream.write(getCompressedImagedataBytes());
			inflaterOutputStream.finish();
			inflaterOutputStream.flush();
			inflaterOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Error reading imagedata",e);
		}
		return baos.toByteArray();
	}
	
	/**
	 * Processes the ImageData object of this PNG.
	 * <br>
	 */
	private void processImageData() {
		if(flagImageDataProcessed) {
			return;
		}
		flagImageDataProcessed=true;
		try {
			Object imageSource=imageData.getImageSource();
			if(imageSource instanceof File) {
				File imageFile=(File)imageSource;
				if(isPngFile(imageFile)) {
					processPngFile(imageFile);
					return;
				}
			}
			createPng();
		} catch (Exception e) {
			throw new RuntimeException("error processing image data",e);
		}
	}

	private void processPngFile(File imageFile) throws IOException {
		if(builder.isReencodePngFilesEnabled()) {
			createPng();
		}else {
			this.baPng=FileTools.readFileAsByteArray(imageFile);
			parsePng();
		}
	}

	private void createPng() {
		if(builder.isPngEncoderEnabled()) {
			PngService.createPngViaPngEncoder(this);
		}else {
			PngService.createPngViaImageIO(this);
			parsePng();
		}
	}
	
	private void parsePng(){
		try {
			chunklist=new ArrayList<Chunk>();

			int index=PNG_SIGNATURE.length;
			ChunkFactory chunkFactory=new ChunkFactory();

			Chunk chunk;
			do {
				chunk=chunkFactory.createChunk(baPng,index);
				chunklist.add(chunk);
				if(chunk instanceof Chunk_IHDR) {
					this.chunk_IHDR=(Chunk_IHDR)chunk;
				}
				index+=chunk.getChunkLength();
			}while(!(chunk instanceof Chunk_IEND));
		} catch (Exception e) {
			throw new RuntimeException("error parsing PNG file",e);
		}
	}

	public void addPng(Png png) {
		if(png.chunklist!=null) {
			throw new RuntimeException("adding apng not supported yet");
		}
		int i=listPng.size();

		Png pngPrevious=null;
		if(i==0) {
			pngPrevious=this;
		}else {
			pngPrevious=listPng.get(i-1);
		}
		int[] rgbIntsPrevious=pngPrevious.getImageData().getRgbInts();
		int[] rgbIntsPngToAdd=png.getImageData().getRgbInts();
		if(Arrays.equals(rgbIntsPrevious,rgbIntsPngToAdd)) {
			this.setDelay(this.getDelay()+png.getDelay());
			logger.trace("skipped identical image");
			return;
		}
		png.setFirstPng(this);
		png.setPreviousPng(pngPrevious);
		listPng.add(png);
	}

	void setFirstPng(Png png) {
		this.firstPng=png;
	}

	Png getFirstPng() {
		return firstPng;
	}

	void setPreviousPng(Png png) {
		this.previousPng=png;
	}

	Png getPreviousPng() {
		return previousPng;
	}

	/**
	 * Writes this PNG to the given file.
	 * <br>All Png objects that were added to this Png object, will be combined to an APNG animation.
	 * <br>If there are no added Png Objects, a PNG file will be created (not an one-image-APNG).
	 * <br>How the (A)PNG is created depends on the settings of the builder that created this Png object.
	 * 
	 * @param file The (A)PNG file to be created
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void savePng(File file) throws FileNotFoundException, IOException {
		try(FileOutputStream fos=new FileOutputStream(file)){
			writePngToStream(fos,null);
		}
	}

	/**
	 * Returns this PNG as bytearray.
	 * <br>All Png objects that were added to this Png object, will be combined to an APNG animation.
	 * <br>If there are no added Png Objects, a PNG file will be created (not an one-image-APNG).
	 * <br>How the (A)PNG is created depends on the settings of the builder that created this Png object.
	 * 
	 * @return PNG as bytearray
	 * @throws IOException
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		writePngToStream(baos,null);
		return baos.toByteArray();
	}

	/**
	 * Returns this PNG as bytearray.
	 * <br>All Png objects that were added to this Png object, will be combined to an APNG animation.
	 * <br>If there are no added Png Objects, a PNG file will be created (not an one-image-APNG).
	 * <br>How the (A)PNG is created depends on the settings of the builder that created this Png object.
	 * <br>While processing, <code>ProgressCallback</code> is calles for each image.
	 * 
	 * @param progressCallback
	 * @return
	 * @throws IOException
	 */
	public byte[] toByteArray(ProgressCallback progressCallback) throws IOException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		writePngToStream(baos,progressCallback);
		return baos.toByteArray();
	}

	/**
	 * Writes this PNG to the given outputstream.
	 * <br>All Png objects that were added to this Png object, will be combined to an APNG animation.
	 * <br>If there are no added Png Objects, a PNG file will be created (not an one-image-APNG).
	 * <br>How the (A)PNG is created depends on the settings of the builder that created this Png object.
	 * <br>While processing, <code>ProgressCallback</code> is calles for each image.
	 * 
	 * @param outputStream
	 * @param progressCallback
	 * @throws IOException
	 */
	private void writePngToStream(OutputStream outputStream,ProgressCallback progressCallback) throws IOException {
		if(progressCallback!=null){
			progressCallback.setProgressStep(1);
		}
		processImageData();
		ArrayList<Chunk> chunklistWork=chunklist;
		if(listPng.size()>0) {
			chunklistWork=createApngChunklist(progressCallback);
		}
		outputStream.write(PNG_SIGNATURE);
		for(int i=0;i<chunklistWork.size();i++) {
			Chunk chunk=chunklistWork.get(i);
			outputStream.write(chunk.toByteArray());
		}
	}

	private ArrayList<Chunk> createApngChunklist(ProgressCallback progressCallback) {
		@SuppressWarnings("unchecked")
		ArrayList<Chunk> apngChunklist=(ArrayList<Chunk>)chunklist.clone();
		int fcTL_sequenceNumber=0;
		Chunk chunk_acTL=new Chunk_acTL(listPng.size()+1,0);
		Chunk chunk_fcTL=new Chunk_fcTL(fcTL_sequenceNumber,getWidth(),getHeight(),0,0,delay,1000,ApngDisposeOperation.NONE,ApngBlendOperation.SOURCE);

		for(int i=0;i<apngChunklist.size();i++) {
			if(apngChunklist.get(i) instanceof Chunk_IDAT) {
				apngChunklist.add(i, chunk_acTL);
				apngChunklist.add(i+1, chunk_fcTL);
				break;
			}
		}

		int index_IEND;
		for(index_IEND=0;index_IEND<apngChunklist.size();index_IEND++) {
			if(apngChunklist.get(index_IEND) instanceof Chunk_IEND) {
				break;
			}
		}

		for(int i=0;i<listPng.size();i++) {
			if(progressCallback!=null) {
				progressCallback.setProgressStep(i+2);
			}
			Png png=listPng.get(i);
			ArrayList<Chunk_IDAT> arraylistIdatChunks=png.getAllIdatChunks();
			chunk_fcTL=new Chunk_fcTL(++fcTL_sequenceNumber,png.getWidth(),png.getHeight(),png.offsetX,png.offsetY,png.getDelay(),1000,ApngDisposeOperation.NONE,ApngBlendOperation.OVER);
			apngChunklist.add(index_IEND++, chunk_fcTL);
			if(i>0) {
				png.getPreviousPng().getImageData().reset();
			}
			for(int x=0;x<arraylistIdatChunks.size();x++) {
				Chunk_IDAT chunk_IDAT=arraylistIdatChunks.get(x);
				Chunk chunk_fdAT=new Chunk_fdAT(++fcTL_sequenceNumber,chunk_IDAT);
				apngChunklist.add(index_IEND++, chunk_fdAT);
			}
		}
		return apngChunklist;
	}

	private ArrayList<Chunk_IDAT> getAllIdatChunks(){
		processImageData();
		ArrayList<Chunk_IDAT> arraylistIdatChunks=new ArrayList<Chunk_IDAT>();
		for(int i=0;i<chunklist.size();i++) {
			if(chunklist.get(i) instanceof Chunk_IDAT) {
				arraylistIdatChunks.add((Chunk_IDAT)chunklist.get(i));
			}
		}
		return arraylistIdatChunks;
	}

	public int getWidth() {
		if(chunk_IHDR==null) {
			throw new RuntimeException("no png parsed yet");
		}
		return chunk_IHDR.getWidth();
	}

	public int getHeight() {
		if(chunk_IHDR==null) {
			throw new RuntimeException("no png parsed yet");
		}
		return chunk_IHDR.getHeight();
	}

	int getOffsetX() {
		return offsetX;
	}

	int getOffsetY() {
		return offsetY;
	}

	void setOffsets(int offsetX, int offsetY) {
		this.offsetX=offsetX;
		this.offsetY=offsetY;
	}

	public void setDelay(int delay) {
		this.delay=delay;
	}

	public int getDelay() {
		return delay;
	}

	/**
	 * Checks if the given bytearray starts with the PNG signature.
	 * 
	 * @param baPng
	 * @return <code>true</code> if the bytearray starts with the PNG signature
	 */
	private static boolean isPngFile(byte[] baPng) {
		return ByteTools.compareBytes(PNG_SIGNATURE,baPng, 0);
	}

	/**
	 * Checks the given file if it is a PNG file.
	 * 
	 * @param fileImage
	 * @return <code>true</code> if the file is a PNG file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	static boolean isPngFile(File fileImage) throws FileNotFoundException, IOException {
		if(fileImage==null) {
			throw new NullPointerException("image");
		}
		if(!fileImage.exists()) {
			throw new FileNotFoundException(fileImage.getAbsolutePath());
		}
		if(fileImage.length()>PNG_SIGNATURE.length) {
			try(InputStream inputStream=new FileInputStream(fileImage)){
				byte[] buffer=new byte[PNG_SIGNATURE.length];
				inputStream.read(buffer);
				return isPngFile(buffer);
			}
		}
		return false;
	}

	void addChunk(Chunk chunk) {
		logger.trace("adding chunk: {}",chunk);
		if(chunklist==null) {
			chunklist=new ArrayList<Chunk>();
		}
		chunklist.add(chunk);
		if(chunk instanceof Chunk_IHDR) {
			chunk_IHDR=(Chunk_IHDR)chunk;
		}
	}

	boolean isApng() {
		processImageData();
		for(int i=0;i<chunklist.size();i++) {
			if(chunklist.get(i) instanceof Chunk_acTL) {
				return true;
			}
		}
		return false;
	}

	ArrayList<ImageData> findAllImagedata(){
		ArrayList<ImageData> allImagedata=new ArrayList<ImageData>();
		allImagedata.add(firstPng.getImageData());
		ArrayList<Png> listPng=firstPng.getListPng();
		for(int k=0;k<listPng.size();k++) {
			allImagedata.add(listPng.get(k).getImageData());
		}
		return allImagedata;
	}

	ArrayList<Png> getListPng() {
		return listPng;
	}

	private int findIndexOfIdatChunk() {
		processImageData();
		for(int i=0;i<chunklist.size();i++) {
			if(chunklist.get(i) instanceof Chunk_IDAT) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Adds some text to this PNG.
	 * <br>A 'keyword' is used to categorize the text.
	 * <br>The PNG specification suggests to use one of these keywords:
	 * <li>Title
	 * <li>Author
	 * <li>Description
	 * <li>Copyright
	 * <li>Creation Time
	 * <li>Software
	 * <li>Disclaimer
	 * <li>Warning
	 * <li>Source
	 * <li>Comment
	 * <br>but 'other keywords may be defined for other purposes'.
	 * <br>Please consult the specification for more information.
	 * 
	 * @param keyword A keyword, with a length of 1 to 79
	 * @param text
	 * @param compressText <code>true</code> to compress the text, <code>false</code>to not compress the text.
	 * @throws UnsupportedEncodingException
	 * @see <a href="https://www.w3.org/TR/PNG/#11keywords">Keywords, Portable Network Graphics (PNG) Specification (Second Edition)</a>
	 */
	public void addText(String keyword, String text, boolean compressText) throws UnsupportedEncodingException {
		int i=findIndexOfIdatChunk();
		if(i==-1) {
			throw new RuntimeException("IDAT chunk not found");
		}
		Chunk chunk;
		if(compressText) {
			chunk=new Chunk_zTXt(keyword,text);
		}else {
			chunk=new Chunk_tEXt(keyword,text);
		}
		chunklist.add(i, chunk);
	}

	/**
	 * Adds some text to this PNG.
	 * <br>A 'keyword' is used to categorize the text.
	 * <br>The PNG specification suggests to use one of these keywords:
	 * <li>Title
	 * <li>Author
	 * <li>Description
	 * <li>Copyright
	 * <li>Creation Time
	 * <li>Software
	 * <li>Disclaimer
	 * <li>Warning
	 * <li>Source
	 * <li>Comment
	 * <br>but 'other keywords may be defined for other purposes'.
	 * <br>Please consult the specification for more information.
	 * <br>This method compresses the text only if that results in smaller data size.
	 * 
	 * @param keyword A keyword, with a length of 1 to 79
	 * @param text
	 * @throws UnsupportedEncodingException
	 */
	public void addText(String keyword, String text) throws UnsupportedEncodingException {
		int i=findIndexOfIdatChunk();
		Chunk_tEXt chunkText=new Chunk_tEXt(keyword,text);
		Chunk_zTXt chunkTextCompressed=new Chunk_zTXt(keyword,text);
		if(chunkTextCompressed.getChunkLength()<chunkText.getChunkLength()) {
			chunklist.add(i, chunkTextCompressed);
		}else {
			chunklist.add(i, chunkText);
		}
	}

	ApngBuilder getBuilder() {
		return builder;
	}
	
	@Override
	public String toString() {
		processImageData();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<chunklist.size();i++) {
			sb.append("chunk "+i+": ");
			sb.append(chunklist.get(i).toString());
			sb.append("\n");
		}
		return sb.toString();
	}

}
