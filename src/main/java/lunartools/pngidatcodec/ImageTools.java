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
		//		BufferedImage bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		BufferedImage bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
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

}
