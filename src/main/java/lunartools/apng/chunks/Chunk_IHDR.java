package lunartools.apng.chunks;

import lunartools.apng.ByteTools;

/** PNG Image Header */
public class Chunk_IHDR extends Chunk{
	public static final String TYPE="IHDR";
	private static final int OFFSET_WIDTH=DATAOFFSET+				0;
	private static final int OFFSET_HEIGHT=DATAOFFSET+				4;
	private static final int OFFSET_BITDEPTH=DATAOFFSET+			8;
	private static final int OFFSET_COLOURTYPE=DATAOFFSET+			9;
	@SuppressWarnings("unused")
	private static final int OFFSET_COMPRESSIONMETHOD=DATAOFFSET+	10;//only compression method 0 (deflate/inflate) defined in international standard
	@SuppressWarnings("unused")
	private static final int OFFSET_FILTERMETHOID=DATAOFFSET+		11;//only filter method 0 defined in International Standard
	private static final int OFFSET_INTERLACEMETHOD=DATAOFFSET+		12;

	Chunk_IHDR(byte[] png, Integer index,Integer length) {
		super(png, index,length);
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
