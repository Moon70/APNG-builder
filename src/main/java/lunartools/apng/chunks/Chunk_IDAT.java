package lunartools.apng.chunks;

public class Chunk_IDAT extends Chunk{
	public static final String TYPE="IDAT";
	
	Chunk_IDAT(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}
	
	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", CHECKSUM="+getChecksum();
	}
	
}
