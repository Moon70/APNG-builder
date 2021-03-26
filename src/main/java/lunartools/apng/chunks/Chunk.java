package lunartools.apng.chunks;

import java.util.Arrays;
import java.util.zip.CRC32;

import lunartools.apng.ByteTools;

/*
 * PNG reference: https://www.w3.org/TR/PNG
 * APNG reference: https://wiki.mozilla.org/APNG_Specification
 * |LENGTH (4 bytes) | CHUNK TYPE (4 bytes) | CHUNK DATA (0 to x bytes) | CRC (4 bytes)|
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
	private int index;
	private int length;

	Chunk() {}
	
	Chunk(byte[] png, Integer index,Integer length) {
		this.data=png;
		this.index=index;
		this.length=length;
		if(!isCrcCorrect()) {
			throw new RuntimeException("CRC error!");
		}
	}
	
	public int getChunkLength() {
		return Chunk.LENGTH_SIZEINBYTES+Chunk.CHUNKTYPE_SIZEINBYTES+length+Chunk.CRC_SIZEINBYTES;
	}
	
	int getDataLength() {
		return length;
	}
	
	void setDataLength(int length) {
		this.length=length;
	}
	
	int getIndex() {
		return index;
	}
	
	private boolean isCrcCorrect() {
		long crc32=getCRC();
		long crc32Calculated=calculateCRC();
		return crc32==crc32Calculated;
	}
	
	/**
	 * @return The CHUNK DATA section of this chunk
	 */
	byte[] getChunkData() {
		int offset=index+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES;
		return Arrays.copyOfRange(data, offset, offset+length);
	}
	
	/**
	 * @return The complete ByteArray of this chunk
	 */
	public byte[] toByteArray() {
		ByteTools.insertLongword(data, calcOffsetOfCrc(), calculateCRC());
		if(index>0) {
			return Arrays.copyOfRange(data, index, index+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES+length+CRC_SIZEINBYTES);
		}
		return data;
	}
	
	/**
	 * A four-byte CRC (Cyclic Redundancy Code) calculated on the preceding bytes in the chunk, including the chunk type field and chunk data fields, but not including the length field. The CRC can be used to check for corruption of the data.
	 * The CRC is always present, even for chunks containing no data.
	 * <br>https://www.w3.org/TR/PNG}
	 * 
	 * @return CRC32 value of: CHUNK TYPE + CHUNK DATA, but not including the LENGTH field
	 */
	private long calculateCRC() {
		CRC32 crc32=new CRC32();
		crc32.update(data, index+LENGTH_SIZEINBYTES,length+CHUNKTYPE_INDEX);
		return crc32.getValue();
	}
	
	private long getCRC() {
		return ByteTools.bytearrayToLongword(data, calcOffsetOfCrc());
	}
	
	private int calcOffsetOfCrc() {
		return index+LENGTH_SIZEINBYTES+CHUNKTYPE_SIZEINBYTES+length;
	}
	
	//TODO: remove method
	public String getChecksum() {
		CRC32 crc32=new CRC32();
		crc32.update(toByteArray());
		return Long.toHexString(crc32.getValue());
	}
}
