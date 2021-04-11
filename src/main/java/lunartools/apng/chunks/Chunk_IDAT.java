package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.apng.ByteTools;

/**
 * The IDAT chunk contains the actual image data which is the output stream of the compression algorithm.
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11IDAT">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_IDAT extends Chunk{
	public static final String TYPE="IDAT";

	Chunk_IDAT(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	public Chunk_IDAT(byte[] byteArray) {
		int length=byteArray.length;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(byteArray);
			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
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
