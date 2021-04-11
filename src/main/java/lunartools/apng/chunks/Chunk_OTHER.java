package lunartools.apng.chunks;

/**
 * To store any chunk that is not yet supported/needed by APNG-builder.
 * 
 * @author Thomas Mattel
 */
public class Chunk_OTHER extends Chunk{
	private String type;

	Chunk_OTHER(byte[] png, Integer index,Integer length, String type) {
		super(png, index,length);
		this.type=type;
	}

	@Override
	public String toString() {
		return type+": length="+getDataLength();
	}

}
