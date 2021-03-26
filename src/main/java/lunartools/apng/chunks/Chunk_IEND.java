package lunartools.apng.chunks;

public class Chunk_IEND extends Chunk{
	public static final String TYPE="IEND";
	
	Chunk_IEND(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}
	
	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", CHECKSUM="+getChecksum();
	}
	
}
