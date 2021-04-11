package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.apng.ByteTools;

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

	Chunk_acTL(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	public Chunk_acTL(Integer num_frames,Integer num_plays) {
		int length=LENGTH_CHUNKDATA;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.longwordToBytearray(num_frames));
			baos.write(ByteTools.longwordToBytearray(num_plays));
			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	private int getNumberOfFrames() {
		return (int)ByteTools.bytearrayToLongword(data,getIndex()+OFFSET_NUM_FRAMES);
	}

	private int getNumberOfPlays() {
		return (int)ByteTools.bytearrayToLongword(data,getIndex()+OFFSET_NUM_PLAYS);
	}

	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", num_frames="+getNumberOfFrames()+", num_plays="+getNumberOfPlays();
	}

}
