package lunartools.pngidatcodec;

import java.awt.image.BufferedImage;

public class ImagedataOptimizer {
	private byte[] imagedata;
	private int offsetX;
	private int offsetY;
	private int width;
	private int height;

	public void createOptimizedImage(BufferedImage bufferedImage,BufferedImage bufferedImageReference) {
		int widthOriginal=bufferedImage.getWidth();
		int heightOriginal=bufferedImage.getHeight();
		
		byte[] bytesImage=ImageTools.getRgbBytesFromBufferedImage(bufferedImage);
		byte[] bytesReference=ImageTools.getRgbBytesFromBufferedImage(bufferedImageReference);
		
		int bytesperpixel=3;
		int top;
		searchTopBorder:
		for(top=0;top<heightOriginal;top++) {
			for(int x=0;x<widthOriginal*bytesperpixel;x++) {
				if(bytesImage[top*widthOriginal*bytesperpixel+x]!=bytesReference[top*widthOriginal*bytesperpixel+x]) {
					break searchTopBorder;
				}
			}
		}
		
		int bottom;
		searchBottomBorder:
		for(bottom=heightOriginal-1;bottom>=0;bottom--) {
			for(int x=0;x<widthOriginal*bytesperpixel;x++) {
				if(bytesImage[bottom*widthOriginal*bytesperpixel+x]!=bytesReference[bottom*widthOriginal*bytesperpixel+x]) {
					break searchBottomBorder;
				}
			}
		}
		
		int left;
		searchLeftBorder:
		for(left=0;left<widthOriginal;left++) {
			for(int y=top;y<=bottom;y++) {
				for(int i=0;i<3;i++) {
					int index=y*widthOriginal*bytesperpixel+left*bytesperpixel+i;
					if(bytesImage[index]!=bytesReference[index]) {
						break searchLeftBorder;
					}
					
				}
			}
		}
		
		int right;
		searchRightBorder:
		for(right=widthOriginal-1;right>=0;right--) {
			for(int y=top;y<=bottom;y++) {
				for(int i=0;i<3;i++) {
					int index=y*widthOriginal*bytesperpixel+right*bytesperpixel+i;
					if(bytesImage[index]!=bytesReference[index]) {
						break searchRightBorder;
					}
					
				}
			}
		}
		
		offsetX=left;
		offsetY=top;
		this.width=right-left+1;
		this.height=bottom-top+1;
		
		byte[] newBytes=new byte[height*width*bytesperpixel];
		int index=0;
		for(int y=top;y<=bottom;y++) {
			for(int x=left;x<=right;x++) {
				for(int i=0;i<bytesperpixel;i++) {
					newBytes[index++]=bytesImage[y*widthOriginal*bytesperpixel+x*3+i];
				}
			}
		}
		this.imagedata=newBytes;
	}

	public byte[] getImagedata() {
		return imagedata;
	}
	
	public int getwidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}
	
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("ApngOptimizer");
		sb.append("\n\tOffsetX: "+offsetX);
		sb.append("\n\tOffsetY: "+offsetY);
		sb.append("\n\tWidth: "+width);
		sb.append("\n\tHeight: "+height);
		return sb.toString();
	}
}
