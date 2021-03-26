package lunartools.apng.chunks;

public class Chunk_OTHER extends Chunk{
	private String type;
	
	Chunk_OTHER(byte[] png, Integer index,Integer length, String type) {
		super(png, index,length);
		this.type=type;
	}
	
	@Override
	public String toString() {
		return type+" length="+getDataLength()+", CHECKSUM="+getChecksum();
	}
	
}
