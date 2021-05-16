package lunartools.apng;

import java.io.ByteArrayOutputStream;

/**
 * Creates a bytearray of uncompressed PNG scanlines from a bytearray of pixeldata.
 * 
 * @author Thomas Mattel
 */
public class PngEncoder {
	private byte[] buffer0;
	private byte[] buffer1;
	private byte[] buffer2;
	private byte[] buffer3;
	private byte[] buffer4;

	private byte[] encodePngType0(byte[] data, int width, int height, int bytesPerPixel) throws Exception {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		final int imageBytesInLine=width*bytesPerPixel;
		buffer0=new byte[imageBytesInLine];
		int offset=0;
		for(int y=0;y<height;y++) {
			System.arraycopy(data, offset, buffer0, 0, imageBytesInLine);
			baos.write(0);
			baos.write(buffer0);
			offset+=imageBytesInLine;
		}
		return baos.toByteArray();
	}

	/**
	 * Applies PNG filter to an bytearray of pixeldata
	 * 
	 * @param data
	 * @param width
	 * @param height
	 * @param bytesPerPixel
	 * @return
	 * @throws Exception
	 */
	byte[] encodePng(byte[] data, int width, int height, int bytesPerPixel) throws Exception {
		if(bytesPerPixel==1) {
			return encodePngType0(data,width,height,bytesPerPixel);
		}
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		FilterScore4 filterScore=new FilterScore4();
		byte[] baLine;
		int typeLine;
		int scoreLine;
		int scoreNext;
		int imageBytesInLine=width*bytesPerPixel;
		buffer0=new byte[imageBytesInLine];
		buffer1=new byte[imageBytesInLine];
		buffer2=new byte[imageBytesInLine];
		buffer3=new byte[imageBytesInLine];
		buffer4=new byte[imageBytesInLine];
		int offset=0;
		for(int y=0;y<height;y++) {
			encodeType4(data,offset,imageBytesInLine,y,bytesPerPixel);
			baLine=buffer4;
			typeLine=4;
			scoreLine=filterScore.calcScore(baLine);

			encodeType3(data,offset,imageBytesInLine,y,bytesPerPixel);
			scoreNext=filterScore.calcScore(buffer3);
			if(scoreNext<scoreLine) {
				scoreLine=scoreNext;
				baLine=buffer3;
				typeLine=3;
			}

			encodeType2(data,offset,imageBytesInLine,y,bytesPerPixel,width);
			scoreNext=filterScore.calcScore(buffer2);
			if(scoreNext<scoreLine) {
				scoreLine=scoreNext;
				baLine=buffer2;
				typeLine=2;
			}

			encodeType1(data,offset,imageBytesInLine,bytesPerPixel);
			scoreNext=filterScore.calcScore(buffer1);
			if(scoreNext<scoreLine) {
				scoreLine=scoreNext;
				baLine=buffer1;
				typeLine=1;
			}

			encodeType0(data,offset,imageBytesInLine);
			scoreNext=filterScore.calcScore(buffer0);
			if(scoreNext<scoreLine) {
				scoreLine=scoreNext;
				baLine=buffer0;
				typeLine=0;
			}

			baos.write(typeLine);
			baos.write(baLine);
			filterScore.addLine(baLine);
			offset+=imageBytesInLine;
		}
		return baos.toByteArray();
	}

	private void encodeType0(byte[] data, int offset, int length) {
		System.arraycopy(data, offset, buffer0, 0, length);
	}

	private void encodeType1(byte[] data, int offset, int length, int bytesPerPixel) {
		int index=0;
		int i;
		for(i=offset;i<offset+bytesPerPixel;i++) {
			buffer1[index++]=(byte)data[i];
		}
		for(;i<offset+length;i++) {
			buffer1[index++]=(byte)(data[i]-data[i-bytesPerPixel]);
		}
	}

	private void encodeType2(byte[] data, int offset, int length, int y,int bytesPerPixel, int width) {
		int index=0;
		if(y==0) {
			for(int i=offset;i<offset+length;i++) {
				buffer2[index++]=data[i];
			}
		}else {
			final int bytesPerLine=width*bytesPerPixel;
			for(int i=offset;i<offset+length;i++) {
				buffer2[index++]=(byte)(data[i]-data[i-bytesPerLine]);
			}
		}
	}

	private void encodeType3(byte[] data, int offset, int length, int y,int bytesPerPixel) {
		int a=0;
		int b=0;
		int index=0;
		int x;
		if(y==0) {
			for(x=offset;x<offset+bytesPerPixel;x++) {
				buffer3[index++]=(data[x]);
			}
			for(;x<offset+length;x++) {
				a=data[x-bytesPerPixel]&0xff;
				buffer3[index++]=(byte)(data[x]-(a>>1));
			}
		}else {
			for(x=offset;x<offset+bytesPerPixel;x++) {
				b=data[x-length]&0xff;
				buffer3[index++]=(byte)(data[x]-(b>>1));
			}
			for(;x<offset+length;x++) {
				a=data[x-bytesPerPixel]&0xff;
				b=data[x-length]&0xff;
				buffer3[index++]=(byte)(data[x]-((a+b)>>1));
			}
		}
	}

	private void encodeType4(byte[] data, int offset, int length, int y,int bytesPerPixel) {
		int a=0;
		int x;
		final int lineOffset=y*length;
		if(y==0) {
			for(int i=0;i<length;i++) {
				x=data[lineOffset+i]&0xff;

				if(i>=bytesPerPixel) {
					a=data[offset+i-bytesPerPixel]&0xff;
					buffer4[i]=(byte)(x-a);
				}else {
					buffer4[i]=(byte)x;
				}
			}
		}else {
			int b=0;
			int c=0;
			for(int i=0;i<length;i++) {
				x=data[lineOffset+i]&0xff;
				b=data[offset+i-length]&0xff;
				
				if(i<bytesPerPixel) {
					buffer4[i]=(byte)(x-b);
				}else {
					a=data[offset+i-bytesPerPixel]&0xff;
					c=data[offset+i-bytesPerPixel-length]&0xff;
					int pa=Math.abs(b-c);
					int pb=Math.abs(a-c);
					int pc=Math.abs(a+b-c-c);
					if(pa<=pb && pa<=pc) {
						buffer4[i]=(byte)(x-a);
					}else if(pb<=pc) {
						buffer4[i]=(byte)(x-b);
					}else {
						buffer4[i]=(byte)(x-c);
					}
				}
			}
		}
	}
}
