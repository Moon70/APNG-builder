package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.apng.ByteTools;

/** APNG Frame Data Chunk */
public class Chunk_fdAT extends Chunk{
	public static final String TYPE="fdAT";
	private static final int OFFSET_SEQUENCE_NUMBER=DATAOFFSET+	 0;

	Chunk_fdAT(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	public Chunk_fdAT(int sequenceNumber,Chunk_IDAT chunk_IDAT) {
		byte[] idatChunkData=chunk_IDAT.getChunkData();
		int length=idatChunkData.length+4;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.longwordToBytearray(sequenceNumber));
			baos.write(idatChunkData);
			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}
	
	/** (unsigned int)   Sequence number of the animation chunk, starting from 0 */
	private int getSequenceNumber() {
		return (int)ByteTools.bytearrayToLongword(data,getIndex()+OFFSET_SEQUENCE_NUMBER);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", sequence_number="+getSequenceNumber()+", CHECKSUM="+getChecksum();
	}
	
}
