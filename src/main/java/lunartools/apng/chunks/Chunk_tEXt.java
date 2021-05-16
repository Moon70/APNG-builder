package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import lunartools.ByteTools;

/**
 * Textual data
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11tEXt">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_tEXt extends Chunk{
	public static final String TYPE="tEXt";
	private static final byte NULL_SEPARATOR=0;
	private static final int lENGTH_OF_NULL_SEPARATOR=1;

	/**
	 * Creates textual data chunk from PNG data.
	 * 
	 * @param png The complete data of a PNG file.
	 * @param index Index to the chunk data.
	 * @param length The chunk length.
	 */
	Chunk_tEXt(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	/**
	 * Create new textual data chunk.
	 * <br>The PNG specification suggests to use one of these keywords:
	 * <li>Title
	 * <li>Author
	 * <li>Description
	 * <li>Copyright
	 * <li>Creation Time
	 * <li>Software
	 * <li>Disclaimer
	 * <li>Warning
	 * <li>Source
	 * <li>Comment
	 * <br>but 'other keywords may be defined for other purposes'.
	 * <br>Please consult the specification for more information.
	 * 
	 * @param keyword The keyword
	 * @param text The textual data
	 * @throws UnsupportedEncodingException
	 * @see <a href="https://www.w3.org/TR/PNG/#11keywords">Keywords, Portable Network Graphics (PNG) Specification (Second Edition)</a>
	 */
	public Chunk_tEXt(String keyword, String text) throws UnsupportedEncodingException {
		if(keyword==null) {
			throw new NullPointerException("keyword");
		}
		if(text==null) {
			throw new NullPointerException("text");
		}
		if(keyword.length()<1 || keyword.length()>79) {
			throw new IllegalArgumentException("allowed keyword length 1 to 79, was "+keyword.length());
		}
		byte[] baKeyword=keyword.getBytes("ISO-8859-1");
		if(indexOfNullSeparator(baKeyword)!=-1) {
			throw new IllegalArgumentException("keyword must not contain a zero byte");
		}
		byte[] baText=text.getBytes("ISO-8859-1");
		if(indexOfNullSeparator(baText)!=-1) {
			throw new IllegalArgumentException("text must not contain a zero byte");
		}
		int length=baKeyword.length+lENGTH_OF_NULL_SEPARATOR+baText.length;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.bLongwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(baKeyword);
			baos.write(NULL_SEPARATOR);
			baos.write(baText);

			baos.write(ByteTools.bLongwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	private int indexOfNullSeparator(byte[] ba) {
		for(int i=0;i<getDataLength();i++) {
			if(ba[i]==NULL_SEPARATOR) {
				return i;
			}
		}
		return -1;
	}

	public String getKeyword() {
		byte[] chunkData=getChunkData();
		int i=indexOfNullSeparator(chunkData);
		if(i==-1) {
			throw new RuntimeException("no null separator in "+TYPE+" chunk");
		}
		try {
			return new String(Arrays.copyOfRange(chunkData, 0, i),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return new String(Arrays.copyOfRange(chunkData, 0, i));
		}
	}

	public String getText() {
		byte[] chunkData=getChunkData();
		int i=indexOfNullSeparator(chunkData);
		if(i==-1) {
			throw new RuntimeException("no null separator in "+TYPE+" chunk");
		}
		try {
			return new String(Arrays.copyOfRange(chunkData, i+lENGTH_OF_NULL_SEPARATOR, chunkData.length),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return new String(Arrays.copyOfRange(chunkData, i+lENGTH_OF_NULL_SEPARATOR, chunkData.length));
		}
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append(TYPE+": length="+getDataLength());
		sb.append(", Keyword="+getKeyword());
		sb.append(", Text="+getText());

		return sb.toString();
	}

}
