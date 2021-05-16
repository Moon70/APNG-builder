package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lunartools.ByteTools;

/**
 * APNG Frame Control Chunk.
 * 
 * @see <a href="https://wiki.mozilla.org/APNG_Specification#.60fcTL.60:_The_Frame_Control_Chunk">APNG Specification</a>
 * @author Thomas Mattel
 */
public class Chunk_fcTL extends Chunk{
	public static final String TYPE="fcTL";
	private static final int LENGTH_CHUNKDATA=26;
	private static final int OFFSET_SEQUENCE_NUMBER=DATAOFFSET+	 0;
	private static final int OFFSET_WIDTH=DATAOFFSET+			 4;
	private static final int OFFSET_HEIGHT=DATAOFFSET+			 8;
	private static final int OFFSET_X_OFFSET=DATAOFFSET+		12;
	private static final int OFFSET_Y_OFFSET=DATAOFFSET+		16;
	private static final int OFFSET_DELAY_NUM=DATAOFFSET+		20;
	private static final int OFFSET_DELAY_DEN=DATAOFFSET+		22;
	private static final int OFFSET_DISPOSE_OP=DATAOFFSET+		24;
	private static final int OFFSET_BLEND_OP=DATAOFFSET+		25;

	public enum ApngDisposeOperation{
		/** APNG specification: no disposal is done on this frame before rendering the next; the contents of the output buffer are left as is. */
		NONE(0),
		/** APNG specification: the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame. */
		BACKGROUND(1),
		/** APNG specification: the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame. */
		PREVIOUS(2);

		private final int value;

		private ApngDisposeOperation(int value) {
			this.value=value;
		}
	}
	
	public enum ApngBlendOperation{
		/** APNG specification: all color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region. */
		SOURCE(0),
		/** APNG specification: the frame should be composited onto the output buffer based on its alpha, using a simple OVER operation as described in the "Alpha Channel Processing" section of the PNG specification [PNG-1.2]. Note that the second variation of the sample code is applicable. */
		OVER(1);

		private final int value;

		private ApngBlendOperation(int value) {
			this.value=value;
		}
	}
	
	/**
	 * Creates frame control chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_fcTL(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Creates new frame control chunk.
	 * 
	 * @param sequenceNumber
	 * @param width
	 * @param height
	 * @param offsetX
	 * @param offsetY
	 * @param delay_num
	 * @param delay_den
	 * @param dispose_op
	 * @param blend_op
	 */
	public Chunk_fcTL(int sequenceNumber,int width,int height,int offsetX, int offsetY,int delay_num,int delay_den,ApngDisposeOperation dispose_op,ApngBlendOperation blend_op) {
		int length=LENGTH_CHUNKDATA;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(ByteTools.bLongwordToBytearray(sequenceNumber));
			baos.write(ByteTools.bLongwordToBytearray(width));
			baos.write(ByteTools.bLongwordToBytearray(height));
			baos.write(ByteTools.bLongwordToBytearray(offsetX));
			baos.write(ByteTools.bLongwordToBytearray(offsetY));
			baos.write(ByteTools.bWordToBytearray(delay_num));
			baos.write(ByteTools.bWordToBytearray(delay_den));
			baos.write((byte)dispose_op.value);
			baos.write((byte)blend_op.value);
			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	/** (unsigned int)   Sequence number of the animation chunk, starting from 0 */
	private int getSequenceNumber() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_SEQUENCE_NUMBER);
	}

	/** (unsigned int)   Width of the following frame */
	private int getWidth() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_WIDTH);
	}

	/** (unsigned int)   Height of the following frame */
	private int getHeight() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_HEIGHT);
	}

	/** (unsigned int)   X position at which to render the following frame */
	private int getXOffset() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_X_OFFSET);
	}

	/** (unsigned int)   Y position at which to render the following frame */
	private int getYOffset() {
		return (int)ByteTools.bBytearrayToLongword(data,getOffset()+OFFSET_Y_OFFSET);
	}

	/** (unsigned short) Frame delay fraction numerator */
	private int getDelayNum() {
		return (int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_DELAY_NUM);
	}

	/** (unsigned short) Frame delay fraction denominator */
	private int getDelayDen() {
		return (int)ByteTools.bBytearrayToWord(data,getOffset()+OFFSET_DELAY_DEN);
	}

	/** (byte)           Type of frame area disposal to be done after rendering this frame */
	private int getDisposeOp() {
		return (int)ByteTools.bytearrayToByte(data,getOffset()+OFFSET_DISPOSE_OP);
	}

	/** (byte)           Type of frame area rendering for this frame  */
	private int getBlendOp() {
		return (int)ByteTools.bytearrayToByte(data,getOffset()+OFFSET_BLEND_OP);
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(TYPE+": length="+getDataLength());
		sb.append("\n\t\tsequence number="+getSequenceNumber());
		sb.append("\n\t\twidth="+getWidth());
		sb.append("\n\t\theight="+getHeight());
		sb.append("\n\t\tx offset="+getXOffset());
		sb.append("\n\t\ty offset="+getYOffset());
		sb.append("\n\t\tdelay num="+getDelayNum());
		sb.append("\n\t\tdelay den="+getDelayDen());
		sb.append("\n\t\tdispose op="+getDisposeOp());
		sb.append("\n\t\tblend op="+getBlendOp());
		return sb.toString();
	}

}
