package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.apng.ByteTools;

/** 
 * Image Header, shall be the first chunk in the PNG datastream.
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

	public static final int COLOURTYPE_GREYSCALE=0;
	public static final int COLOURTYPE_TRUECOLOUR=2;
	public static final int COLOURTYPE_INDEXEDCOLOUR=3;
	public static final int COLOURTYPE_GREYSCALE_WITH_ALPHA=4;
	public static final int COLOURTYPE_TRUECOLOUR_WITH_ALPHA=6;

	Chunk_IHDR(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}


	public Chunk_IHDR(int width, int height, int bitdepth, int colourtype) {
		int length=LENGTH_CHUNKDATA;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.longwordToBytearray(width));
			baos.write(ByteTools.longwordToBytearray(height));
			baos.write((byte)bitdepth);
			baos.write((byte)colourtype);
			baos.write(0);//compression method
			baos.write(0);//filter method
			baos.write(0);//interlace method

			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	public int getWidth() {
		return (int)ByteTools.bytearrayToLongword(data,getIndex()+OFFSET_WIDTH);
	}

	public int getHeight() {
		return (int)ByteTools.bytearrayToLongword(data,getIndex()+OFFSET_HEIGHT);
	}

	private int getBitDepth() {
		return (int)ByteTools.bytearrayToByte(data,getIndex()+OFFSET_BITDEPTH);
	}

	private int getColourType() {
		return (int)ByteTools.bytearrayToByte(data,getIndex()+OFFSET_COLOURTYPE);
	}

	private String getColourTypeAsString() {
		switch(getColourType()) {
		case 0:
			return "Greyscale";
		case 2:
			return "Truecolour";
		case 3:
			return "Indexed colour";
		case 4:
			return "Greyscale with alpha";
		case 6:
			return "Truecolour with alpha";
		default:
			return "UNKNOWN";
		}
	}

	private int getInterlaceMethod() {
		return (int)ByteTools.bytearrayToByte(data,getIndex()+OFFSET_INTERLACEMETHOD);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", width="+getWidth()+",  height="+getHeight()
		+", bitDepth="+getBitDepth()+", colourType="+getColourTypeAsString()
		+", interlaceMethod="+getInterlaceMethod()
		;
	}

}
