package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;

/** 
 * APNG Animation Control Chunk.
 * 
 * @see <a href="https://wiki.mozilla.org/APNG_Specification#.60acTL.60:_The_Animation_Control_Chunk">APNG Specification</a>
 * @author Thomas Mattel
 */
public class Chunk_acTL extends Chunk{
	public static final String TYPE="acTL";
	private static final int LENGTH_CHUNKDATA=8;
	private static final int OFFSET_NUM_FRAMES=DATAOFFSET+	0;
	private static final int OFFSET_NUM_PLAYS=DATAOFFSET+	4;

	/**
	 * Creates animation control chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_acTL(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Creates new animation control chunk.
	 * 
	 * @param num_frames Number of frames of this animation
	 * @param num_plays Number of plays of this animation
	 */
	public Chunk_acTL(Integer num_frames,Integer num_plays) {
		int length=LENGTH_CHUNKDATA;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bLongwordToBytearray(num_frames));
			baos.write(ByteTools.bLongwordToBytearray(num_plays));
			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	private int getNumberOfFrames() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_NUM_FRAMES);
	}

	private int getNumberOfPlays() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_NUM_PLAYS);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", num_frames="+getNumberOfFrames()+", num_plays="+getNumberOfPlays();
	}

}
