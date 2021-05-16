package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;

/** 
 * APNG Frame Data Chunk.
 * 
 * @see <a href="https://wiki.mozilla.org/APNG_Specification#.60fdAT.60:_The_Frame_Data_Chunk">APNG Specification</a>
 * @author Thomas Mattel
 */
public class Chunk_fdAT extends Chunk{
	public static final String TYPE="fdAT";
	private static final int OFFSET_SEQUENCE_NUMBER=DATAOFFSET+	 0;

	/**
	 * Creates frame data chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_fdAT(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Creates new frame data chunk.
	 * 
	 * @param sequenceNumber The sequence number of this image
	 * @param chunk_IDAT The IDAT chunk that contains the image data used for this frame data chunk
	 */
	public Chunk_fdAT(int sequenceNumber,Chunk_IDAT chunk_IDAT) {
		byte[] idatChunkData=chunk_IDAT.getChunkData();
		int length=idatChunkData.length+4;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bLongwordToBytearray(sequenceNumber));
			baos.write(idatChunkData);
			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/** (unsigned int)   Sequence number of the animation chunk, starting from 0 */
	private int getSequenceNumber() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_SEQUENCE_NUMBER);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", sequence_number="+getSequenceNumber();
	}

}
