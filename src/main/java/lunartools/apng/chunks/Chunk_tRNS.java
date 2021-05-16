package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;
import lunartools.apng.chunks.Chunk_IHDR.ColourType;

/**
 * Transparency
 * <br>Depending on colour type, it contains:
 * <li>Grey sample value (2 bytes) for greyscale images
 * <li>Red Green Blue sample values (6 bytes) for truecolour images
 * <li>Alpha value for palette images (1 byte per palette entry)
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11tRNS">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_tRNS extends Chunk{
	public static final String TYPE="tRNS";
	private static final int OFFSET_RED=DATAOFFSET+				0;
	private static final int OFFSET_GREEN=DATAOFFSET+			2;
	private static final int OFFSET_BLUE=DATAOFFSET+			4;
	private static final int OFFSET_GREY=DATAOFFSET+			0;

	private ColourType colorType;

	/**
	 * Creates Transparency chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_tRNS(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Creates a Transparency Chunk for colour type 0 (greyscale)
	 * 
	 * @param grey An 8 bit greyscale value
	 */
	public Chunk_tRNS(int grey) {
		colorType=ColourType.GREYSCALE;
		int length=2;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bWordToBytearray(grey & 0xff));

			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/**
	 * Creates a Transparency Chunk for colour type 2 (truecolour)
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public Chunk_tRNS(int red, int green, int blue) {
		colorType=ColourType.TRUECOLOUR;
		int length=6;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bWordToBytearray(red));
			baos.write(ByteTools.bWordToBytearray(green));
			baos.write(ByteTools.bWordToBytearray(blue));

			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/**
	 * Creates a Transparency Chunk for colour type 3 (indexed colour)
	 * 
	 * @param alphaPalette
	 */
	public Chunk_tRNS(int[] alphaPalette) {
		colorType=ColourType.INDEXEDCOLOUR;
		int length=alphaPalette.length;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			for(int i=0;i<alphaPalette.length;i++) {
				baos.write((byte)alphaPalette[i]);
			}
			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/**
	 * Returns the 24 bit transparent colour value for a greyscale or truecolour image.
	 * 
	 * @return The 24 bit transparent colour value for a greyscale or truecolour image
	 */
	public int getTransparentColour() {
		switch(colorType) {
		case GREYSCALE:
			int grey=(int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_GREY);
			return (grey<<16)|(grey<<8)|grey;
		case TRUECOLOUR:
			int red=(int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_RED);
			int green=(int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_GREEN);
			int blue=(int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_BLUE);
			return (red<<16)|(green<<8)|blue;
		default:
			throw new RuntimeException("neither greyscale nor truecolour "+TYPE+" chunk!");
		}
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(TYPE+": length="+getDataLength());
		if(colorType!=null) {
			switch(colorType) {
			case GREYSCALE:
				sb.append(", transparent greyscale colour="+Integer.toHexString(getTransparentColour()));
				break;
			case TRUECOLOUR:
				sb.append(", transparent truecolour="+Integer.toHexString(getTransparentColour()));
				break;
			case INDEXEDCOLOUR:
				sb.append(", palette-NumberOfEntries="+getDataLength());
				break;
			default:
				sb.append(", unknown");
			}
		}else {
			sb.append(", unknown");
		}
		return sb.toString();
	}

}
