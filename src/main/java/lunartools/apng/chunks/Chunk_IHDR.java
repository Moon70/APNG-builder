package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;

/** 
 * Image Header, shall be the first chunk in the PNG data stream.
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11IHDR">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_IHDR extends Chunk{
	public static final String TYPE="IHDR";
	private static final int LENGTH_CHUNKDATA=13;
	private static final int OFFSET_WIDTH=DATAOFFSET+				0;
	private static final int OFFSET_HEIGHT=DATAOFFSET+				4;
	private static final int OFFSET_BITDEPTH=DATAOFFSET+			8;
	private static final int OFFSET_COLOURTYPE=DATAOFFSET+			9;
	@SuppressWarnings("unused")
	private static final int OFFSET_COMPRESSIONMETHOD=DATAOFFSET+	10;//only compression method 0 (deflate/inflate) defined in international standard
	@SuppressWarnings("unused")
	private static final int OFFSET_FILTERMETHOID=DATAOFFSET+		11;//only filter method 0 defined in International Standard
	private static final int OFFSET_INTERLACEMETHOD=DATAOFFSET+		12;

	public enum ColourType{
		GREYSCALE(				0,	"Greyscale"),
		TRUECOLOUR(				2,	"Truecolour"),
		INDEXEDCOLOUR(			3,	"Indexed colour"),
		GREYSCALE_WITH_ALPHA(	4,	"Greyscale with alpha"),
		TRUECOLOUR_WITH_ALPHA(	6,	"Truecolour with alpha");

		private final int value;
		private String name;
		
		private ColourType(int value, String name) {
			this.value=value;
			this.name=name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Creates Image Header chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_IHDR(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Creates new Image Header.
	 * 
	 * @param width
	 * @param height
	 * @param bitdepth
	 * @param colourtype
	 */
	public Chunk_IHDR(int width, int height, int bitdepth, ColourType colourtype) {
		setDataLength(LENGTH_CHUNKDATA);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(LENGTH_CHUNKDATA));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bLongwordToBytearray(width));
			baos.write(ByteTools.bLongwordToBytearray(height));
			baos.write((byte)bitdepth);
			baos.write((byte)colourtype.value);
			baos.write(0);//compression method
			baos.write(0);//filter method
			baos.write(0);//interlace method

			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	public int getWidth() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_WIDTH);
	}

	public int getHeight() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_HEIGHT);
	}

	private int getBitDepth() {
		return (int)ByteTools.bytearrayToByte(data,getOffset()+OFFSET_BITDEPTH);
	}

	private ColourType getColourType() {
		final int colourType=(int)ByteTools.bytearrayToByte(data,getOffset()+OFFSET_COLOURTYPE);
		if(colourType==			ColourType.GREYSCALE.value) {
			return 				ColourType.GREYSCALE;
		}else if(colourType==	ColourType.TRUECOLOUR.value) {
			return 				ColourType.TRUECOLOUR;
		}else if(colourType==	ColourType.INDEXEDCOLOUR.value) {
			return 				ColourType.INDEXEDCOLOUR;
		}else if(colourType==	ColourType.GREYSCALE_WITH_ALPHA.value) {
			return 				ColourType.GREYSCALE_WITH_ALPHA;
		}else if(colourType==	ColourType.TRUECOLOUR_WITH_ALPHA.value) {
			return 				ColourType.TRUECOLOUR_WITH_ALPHA;
		}else {
			return null;
		}
	}

	private int getInterlaceMethod() {
		return (int)ByteTools.bytearrayToByte(data,getOffset()+OFFSET_INTERLACEMETHOD);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", width="+getWidth()+",  height="+getHeight()
		+", bitDepth="+getBitDepth()+", colourType="+getColourType()
		+", interlaceMethod="+getInterlaceMethod()
		;
	}

}
