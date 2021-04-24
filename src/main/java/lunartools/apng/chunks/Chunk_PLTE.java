package lunartools.apng.chunks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lunartools.apng.ByteTools;
import lunartools.apng.Color;

/**
 * Palette
 * <br>Contains from 1 to 256 palette entries, each a three-byte series of the form Red Green Blue.
 * 
 * @see <a href="https://www.w3.org/TR/PNG/#11PLTE">Portable Network Graphics (PNG) Specification (Second Edition)</a>
 * @author Thomas Mattel
 */
public class Chunk_PLTE extends Chunk{
	public static final String TYPE="PLTE";

	Chunk_PLTE(byte[] png, Integer index,Integer length) {
		super(png, index,length);
	}
	
	public Chunk_PLTE(ArrayList<Color> palette) {
		int length=palette.size()*3;
		setDataLength(length);
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			baos.write(ByteTools.longwordToBytearray(length));
			baos.write(TYPE.getBytes());
			for(int i=0;i<palette.size();i++) {
				Color color=palette.get(i);
				baos.write((byte)color.getRed());
				baos.write((byte)color.getGreen());
				baos.write((byte)color.getBlue());
			}
			baos.write(ByteTools.longwordToBytearray(0));//CRC, calculated later
			data=baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Could not create "+TYPE+" bytearray",e);
		}
	}
		
	@Override
	public String toString() {
		return TYPE+": length="+getDataLength()+", size[numberOfColours]="+getDataLength()/3
		;
	}

}
