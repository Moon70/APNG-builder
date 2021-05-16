package lunartools.apng.chunks;

/**
 * Stores any chunk that is not yet supported/needed by APNG-builder.
 * 
 * @author Thomas Mattel
 */
class Chunk_OTHER extends Chunk{
	private String type;

	/**
	 * Creates an object to store data of an unsupported chunk.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 * @param type The 4-byte chunk type.
	 */
	Chunk_OTHER(byte[] png, Integer index,Integer length, String type) {
		super(png, index,length);
		this.type=type;
	}

	@Override
	public String toString() {
		return type+": length="+getDataLength();
	}

}
