package lunartools.apng.chunks;

import java.util.Arrays;
import java.util.zip.CRC32;

import lunartools.ByteTools;

/**
 * An (A)PNG chunk of a PNG file.
 * 
 * @see <a href="https://www.w3.org/TR/PNG">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @see <a href="https://wiki.mozilla.org/APNG_Specification">APNG Specification</a>
 * @author Thomas Mattel
 */
public abstract class Chunk {
	public static final int LENGTH_SIZEINBYTES=4;
	public static final int CHUNKTYPE_SIZEINBYTES=4;
	public static final int CRC_SIZEINBYTES=4;
	public static final int DATAOFFSET=LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES;
	//	public static final int LENGTH_INDEX=0;
	public static final int CHUNKTYPE_INDEX=4;
	//	public static final int CHUNKDATA_INDEX=8;

	byte[] data;
	private int offset;
	private int length;

	Chunk() {}

	/**
	 * Creates Image Header chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param offset Offset to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk(byte[] png, Integer offset,Integer length) {
		this.data=png;
		this.offset=offset;
		this.length=length;
		if(!isCrcValid()) {
			throw new RuntimeException("CRC error!");
		}
	}

	/** @return The total chunk length */
	public int getChunkLength() {
		return Chunk.LENGTH_SIZEINBYTES+Chunk.CHUNKTYPE_SIZEINBYTES+length+Chunk.CRC_SIZEINBYTES;
	}

	/** @return The length of the data part of this chunk, this is not the total chunk length */
	int getDataLength() {
		return length;
	}

	/** Set length of the data part of the chunk, this is not the total chunk length */
	void setDataLength(int length) {
		this.length=length;
	}

	/** Offset to the chunk data within the bytearray data */
	int getOffset() {
		return offset;
	}

	private boolean isCrcValid() {
		long crc32=getCRC();
		long crc32Calculated=calculateCRC();
		return crc32==crc32Calculated;
	}

	/**
	 * @return The CHUNK DATA section of this chunk
	 */
	public byte[] getChunkData() {
		int offsetChunkdata=offset+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES;
		return Arrays.copyOfRange(data, offsetChunkdata, offsetChunkdata+length);
	}

	/**
	 * @return The complete ByteArray of this chunk
	 */
	public byte[] toByteArray() {
		ByteTools.bWriteLongwordToBytearray(data, calculateOffsetOfCRC(), calculateCRC());
		if(offset>0) {
			return Arrays.copyOfRange(data, offset, offset+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES+length+CRC_SIZEINBYTES);
		}
		return data;
	}

	/**
	 * A four-byte CRC (Cyclic Redundancy Code) calculated on the preceding bytes in the chunk, including the chunk type field and chunk data fields, but not including the length field. The CRC can be used to check for corruption of the data.
	 * The CRC is always present, even for chunks containing no data.
	 * 
	 * @return CRC32 value of: CHUNK TYPE + CHUNK DATA, but not including the LENGTH field
	 */
	private long calculateCRC() {
		CRC32 crc32=new CRC32();
		crc32.update(data, offset+LENGTH_SIZEINBYTES,length+CHUNKTYPE_INDEX);
		return crc32.getValue();
	}

	private long getCRC() {
		return ByteTools.bBytearrayToLongword(data, calculateOffsetOfCRC());
	}

	private int calculateOffsetOfCRC() {
		return offset+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES+length;
	}

}
