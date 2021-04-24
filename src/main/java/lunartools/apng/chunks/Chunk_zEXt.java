package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import lunartools.apng.ByteTools;

/**
 * Compressed textual data
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11zTXt">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_zEXt extends Chunk{
	public static final String TYPE="zEXt";
	private static final byte NULL_SEPARATOR=0;
	private static final byte COMPRESSIONMETHOD_DEFLATE=0;
	private static final int lENGTH_OF_NULL_SEPARATOR=1;
	private static final int lENGTH_OF_OMPRESSOR_METHOD=1;

	Chunk_zEXt(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}

	public Chunk_zEXt(String keyword, String text) throws UnsupportedEncodingException {
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
		byte[] textCompressed=compress(baText);
		int length=baKeyword.length+lENGTH_OF_NULL_SEPARATOR+lENGTH_OF_OMPRESSOR_METHOD+textCompressed.length;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			baos.write(baKeyword);
			baos.write(NULL_SEPARATOR);
			baos.write(COMPRESSIONMETHOD_DEFLATE);
			baos.write(textCompressed);

			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}

	private byte[] compress(byte[] bytes) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			Deflater deflater=new Deflater(Deflater.BEST_COMPRESSION);
			DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(baos,deflater);
			deflaterOutputStream.write(bytes);
			deflaterOutputStream.finish();
			deflaterOutputStream.flush();
			deflaterOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Error compressing zEXt text",e);
		}
		return baos.toByteArray();
	}

	private byte[] decompress(byte[] bytes) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			Inflater inflater=new Inflater();
			InflaterOutputStream inflaterOutputStream=new InflaterOutputStream(baos,inflater);
			inflaterOutputStream.write(bytes);
			inflaterOutputStream.finish();
			inflaterOutputStream.flush();
			inflaterOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Error decompressing zEXt text",e);
		}
		return baos.toByteArray();
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
			throw new RuntimeException("no null separator in zEXt chunk");
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
			throw new RuntimeException("no null separator in zEXt chunk");
		}
		try {
			return new String(decompress(Arrays.copyOfRange(chunkData, i+lENGTH_OF_NULL_SEPARATOR+lENGTH_OF_OMPRESSOR_METHOD, chunkData.length)),"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			return new String(decompress(Arrays.copyOfRange(chunkData, i+lENGTH_OF_NULL_SEPARATOR+lENGTH_OF_OMPRESSOR_METHOD, chunkData.length)));
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
