package lunartools.pngidatcodec;

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

public class ImageTools {

	public static byte[] changeIntRGBtoByteRGB(int[] intbuffer) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		for(int i=0;i<intbuffer.length;i++) {
			int pixel=intbuffer[i];
			int red=(pixel>>16)&0xff;
			int green=(pixel>>8)&0xff;
			int blue=pixel&0xff;
			baos.write(red);
			baos.write(green);
			baos.write(blue);
		}
		return baos.toByteArray();
	}

	private static void changeByteBGRtoByteRGB(byte[] data) {
		byte b;
		for(int i=0;i<data.length;i+=3) {
			b=data[i];
			data[i]=data[i+2];
			data[i+2]=b;
		}
	}

	private static int[] changeByteBGRtoIntRGB(byte[] data) {
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

	public static BufferedImage createBufferedImageFromFile(File file) {
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

	public static byte[] getRgbBytesFromBufferedImage(BufferedImage bufferedImage) {
		DataBuffer databuffer=bufferedImage.getRaster().getDataBuffer();
		if(databuffer instanceof DataBufferInt) {
			int[] intImagedata=((DataBufferInt)databuffer).getData();
			byte[] byteImagedata=changeIntRGBtoByteRGB(intImagedata);
			return byteImagedata;
		}else if(databuffer instanceof DataBufferByte) {
			byte[] byteImagedata=((DataBufferByte)databuffer).getData().clone();
			changeByteBGRtoByteRGB(byteImagedata);
			return byteImagedata;
		}else {
			throw new RuntimeException("databuffer not supported: "+databuffer.getClass().getName());
		}
	}

	public static int[] getRgbIntsFromBufferedImage(BufferedImage bufferedImage) {
		DataBuffer databuffer=bufferedImage.getRaster().getDataBuffer();
		if(databuffer instanceof DataBufferInt) {
			int[] intImagedata=((DataBufferInt)databuffer).getData();
			for(int i=0;i<intImagedata.length;i++) {
				intImagedata[i]=intImagedata[i]&0xffffff;
			}
			return intImagedata;
		}else if(databuffer instanceof DataBufferByte) {
			byte[] byteImagedata=((DataBufferByte)databuffer).getData().clone();
			int[] intImagedata=changeByteBGRtoIntRGB(byteImagedata);
			return intImagedata;
		}else {
			throw new RuntimeException("databuffer not supported: "+databuffer.getClass().getName());
		}
	}

}
