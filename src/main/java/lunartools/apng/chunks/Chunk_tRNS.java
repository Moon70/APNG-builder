package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.apng.ByteTools;

/**
 * Transparency
 * <br>Depending on colour type, it contains:
 * <li>Grey sample value (2 bytes) for greyscale images <b><u>(not implemented yet)</u></b>
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

	private int colorType;
	
	Chunk_tRNS(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}
	
	/**
	 * Creates a tRNS Chunk for colour type 2 (truecolour)
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 */
	public Chunk_tRNS(int red, int green, int blue) {
		colorType=Chunk_IHDR.COLOURTYPE_TRUECOLOUR;
		int length=6;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.wordToBytearray(red));
			baos.write(ByteTools.wordToBytearray(green));
			baos.write(ByteTools.wordToBytearray(blue));

			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/**
	 * Creates a tRNS Chunk for colour type 3 (indexed colour)
	 * 
	 * @param alphaPalette
	 */
	public Chunk_tRNS(int[] alphaPalette) {
		colorType=Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR;
		int length=alphaPalette.length;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			for(int i=0;i<alphaPalette.length;i++) {
				baos.write((byte)alphaPalette[i]);
			}
			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	public int getTransparentColour() {
		if(colorType!=Chunk_IHDR.COLOURTYPE_TRUECOLOUR) {
			throw new RuntimeException("non a truecolour tRNS chunk!");
		}
		int red=(int)ByteTools.bytearrayToWord(data,getIndex()+OFFSET_RED);
		int green=(int)ByteTools.bytearrayToWord(data,getIndex()+OFFSET_GREEN);
		int blue=(int)ByteTools.bytearrayToWord(data,getIndex()+OFFSET_BLUE);
		return (red<<16)|(green<<8)|blue;
	}
	
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(TYPE+": length="+getDataLength());
		switch(colorType) {
		case Chunk_IHDR.COLOURTYPE_TRUECOLOUR:
			sb.append(", TrueColour-transparentColour="+getTransparentColour());
			break;
		case Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR:
			sb.append(", palette-NumberOfEntries="+getDataLength());
			break;
		default:
			sb.append(", UNKNOWN");
		}
		return sb.toString();
	}
	
}
