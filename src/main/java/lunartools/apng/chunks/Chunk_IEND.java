package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;

/** 
 * The IEND chunk marks the end of the PNG datastream. The chunk's data field is empty. 
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11IEND">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_IEND extends Chunk{
	public static final String TYPE="IEND";

	/**
	 * Creates end chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_IEND(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/** Creates new end chunk */
	public Chunk_IEND() {
		int length=0;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength();
	}

}
