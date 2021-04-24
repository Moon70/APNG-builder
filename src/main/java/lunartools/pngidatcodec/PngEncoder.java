package lunartools.pngidatcodec;

import java.io.ByteArrayOutputStream;

/**
 * Creates a bytearray of uncompressed PNG scanlines from a BufferedImage.
 * 
 * @author Thomas Mattel
 */
public class PngEncoder {

	public byte[] encodePng(byte[] data, int width, int height, int bytesPerPixel) throws Exception {
		FilterScore filterScore=new FilterScore4();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		byte[] baLine;
		int typeLine;
		int scoreLine;
		byte[] baNext;
		int scoreNext;
		int imageBytesInLine=width*bytesPerPixel;
		for(int y=0;y<height;y++) {
			int offset=y*imageBytesInLine;
			if(bytesPerPixel==1) {
				baLine=encodeType0(data,offset,imageBytesInLine);
				typeLine=0;
			}else {
				baLine=encodeType4(data,offset,imageBytesInLine,y,bytesPerPixel);
				typeLine=4;
				scoreLine=filterScore.calcScore(baLine);

				baNext=encodeType3(data,offset,imageBytesInLine,y,bytesPerPixel);
				scoreNext=filterScore.calcScore(baNext);
				if(scoreNext<scoreLine) {
					scoreLine=scoreNext;
					baLine=baNext;
					typeLine=3;
				}

				baNext=encodeType2(data,offset,imageBytesInLine,y,bytesPerPixel,width);
				scoreNext=filterScore.calcScore(baNext);
				if(scoreNext<scoreLine) {
					scoreLine=scoreNext;
					baLine=baNext;
					typeLine=2;
				}

				baNext=encodeType1(data,offset,imageBytesInLine,bytesPerPixel);
				scoreNext=filterScore.calcScore(baNext);
				if(scoreNext<scoreLine) {
					scoreLine=scoreNext;
					baLine=baNext;
					typeLine=1;
				}

				baNext=encodeType0(data,offset,imageBytesInLine);
				scoreNext=filterScore.calcScore(baNext);
				if(scoreNext<scoreLine) {
					scoreLine=scoreNext;
					baLine=baNext;
					typeLine=0;
				}
			}

			//System.out.println("line "+y+" "+typeLine+" "+scoreLine);
			baos.write(typeLine);
			baos.write(baLine);
			filterScore.addLine(baLine);
		}
		return baos.toByteArray();
	}

	private byte[] encodeType0(byte[] data, int offset, int length) {
		byte[] ba=new byte[length];
		for(int x=0;x<length;x++) {
			ba[x]=data[offset+x];
		}
		return ba;
	}

	private byte[] encodeType1(byte[] data, int offset, int length, int bytesPerPixel) {
		byte[] ba=new byte[length];
		for(int b=0;b<length;b++) {
			int x=data[offset+b];
			if(x<0) x+=256;
			if(b>2) {
				int a=data[offset+b-bytesPerPixel];
				if(a<0) a+=256;
				x-=a;
			}
			ba[b]=(byte)x;
		}
		return ba;
	}

	private byte[] encodeType2(byte[] data, int offset, int length, int y,int bytesPerPixel, int width) {
		byte[] ba=new byte[length];
		for(int x=0;x<length;x++) {
			int xx=data[offset+x];
			if(xx<0) xx+=256;
			if(y>0) {
				int b=data[offset+x-width*bytesPerPixel];
				if(b<0) b+=256;
				xx-=b;
			}
			ba[x]=(byte)xx;
		}
		return ba;
	}

	private byte[] encodeType3(byte[] data, int offset, int length, int y,int bytesPerPixel) {
		byte[] ba=new byte[length];
		for(int x=0;x<length;x++) {
			int xx=data[offset+x];
			if(xx<0) xx+=256;

			int a=0;
			int b=0;

			if(x>2) {
				a=data[offset+x-bytesPerPixel];
				if(a<0) a+=256;
			}

			if(y>0) {
				b=data[offset+x-length];
				if(b<0) b+=256;
			}

			xx-=(a+b)>>1;

				ba[x]=(byte)xx;
		}
		return ba;
	}

	private byte[] encodeType4(byte[] data, int offset, int length, int y,int bytesPerPixel) {
		byte[] ba=new byte[length];
		for(int x=0;x<length;x++) {
			int xx=data[y*length+x];
			if(xx<0) xx+=256;

			int a=0;
			int b=0;
			int c=0;

			if(x>2) {
				a=data[offset+x-bytesPerPixel];
				if(a<0) a+=256;
			}

			if(y>0) {
				b=data[offset+x-length];
				if(b<0) b+=256;
			}

			if(x>2 && y>0) {
				c=data[offset+x-bytesPerPixel-length];
				if(c<0) c+=256;
			}

			int p=a+b-c;
			int pa=Math.abs(p-a);
			int pb=Math.abs(p-b);
			int pc=Math.abs(p-c);
			if(pa<=pb && pa<=pc) {
				xx-=a;
			}else if(pb<=pc) {
				xx-=b;
			}else {
				xx-=c;
			}

			ba[x]=(byte)xx;
		}
		return ba;
	}

}
