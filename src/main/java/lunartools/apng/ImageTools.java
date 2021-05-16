package lunartools.apng;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.swing.JFrame;

/**
 * Just some helper methods, some very unoptimized...
 * 
 * @author Thomas Mattel
 */
public class ImageTools {

	static byte[] convertIntGreyscaleToByteGreyscale(int[] intbuffer) {
		byte[] bytes=new byte[intbuffer.length];
		for(int i=0;i<intbuffer.length;i++) {
			bytes[i]=(byte)(intbuffer[i]&0xff);
		}
		return bytes;
	}

	static byte[] convertIntRGBtoByteRGB(int[] intbuffer) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		for(int i=0;i<intbuffer.length;i++) {
			int pixel=intbuffer[i];
			baos.write((pixel>>16)&0xff);
			baos.write((pixel>>8)&0xff);
			baos.write(pixel&0xff);
		}
		return baos.toByteArray();
	}

	private static void convertByteBGRtoByteRGB(byte[] data) {
		byte b;
		for(int i=0;i<data.length;i+=3) {
			b=data[i];
			data[i]=data[i+2];
			data[i+2]=b;
		}
	}

	private static int[] convertByteBGRtoIntRGB(byte[] data) {
		int[] dataInt=new int[data.length/3];
		int rgb;
		int b;
		for(int i=0;i<data.length;i+=3) {
			b=data[i+2];
			if(b<0) b+=256;
			rgb=b<<16;
			b=data[i+1];
			if(b<0) b+=256;
			rgb+=b<<8;
			b=data[i];
			if(b<0) b+=256;
			rgb+=b;
			dataInt[i/3]=rgb;
		}
		return dataInt;
	}

	static BufferedImage createBufferedImageFromFile(File file) {
		Image image=Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());

		JFrame jframe=new JFrame();

		MediaTracker mediaTracker = new MediaTracker(jframe);
		mediaTracker.addImage(image,0);
		try{
			mediaTracker.waitForAll();
		}catch (InterruptedException e){
			throw new RuntimeException("Resource >"+file+"< could not be loaded!",e);
		}

		int width=image.getWidth(null);
		int height=image.getHeight(null);
		BufferedImage bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		//BufferedImage bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		bufferedImage.getGraphics().drawImage(image,0,0,jframe);
		return bufferedImage;
	}

	static byte[] getRgbBytesFromBufferedImage(BufferedImage bufferedImage) {
		DataBuffer databuffer=bufferedImage.getRaster().getDataBuffer();
		if(databuffer instanceof DataBufferInt) {
			int[] intImagedata=((DataBufferInt)databuffer).getData();
			byte[] byteImagedata=convertIntRGBtoByteRGB(intImagedata);
			return byteImagedata;
		}else if(databuffer instanceof DataBufferByte) {
			byte[] byteImagedata=((DataBufferByte)databuffer).getData().clone();
			convertByteBGRtoByteRGB(byteImagedata);
			return byteImagedata;
		}else {
			throw new RuntimeException("databuffer not supported: "+databuffer.getClass().getName());
		}
	}

	static int[] getRgbIntsFromBufferedImage(BufferedImage bufferedImage) {
		DataBuffer databuffer=bufferedImage.getRaster().getDataBuffer();
		if(databuffer instanceof DataBufferInt) {
			int[] intImagedata=((DataBufferInt)databuffer).getData();
			for(int i=0;i<intImagedata.length;i++) {
				intImagedata[i]=intImagedata[i]&0xffffff;
			}
			return intImagedata;
		}else if(databuffer instanceof DataBufferByte) {
			byte[] byteImagedata=((DataBufferByte)databuffer).getData().clone();
			int[] intImagedata=convertByteBGRtoIntRGB(byteImagedata);
			return intImagedata;
		}else {
			throw new RuntimeException("databuffer not supported: "+databuffer.getClass().getName());
		}
	}

}
