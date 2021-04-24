package lunartools.apng;

import lunartools.pngidatcodec.ImageTools;

public class ImagedataOptimizer {
	private int[] intImage;
	private byte[] imagedata;
	private int offsetX;
	private int offsetY;
	private int width;
	private int height;

	public void optimizeImage(Png png) {
		this.offsetX=0;
		this.offsetY=0;

		ImageData imageData=png.getImageData();
		this.width=imageData.getWidth();
		this.height=imageData.getHeight();

		Png pngReference=png.getPreviousPng();
		ImageData imageDataReference=pngReference.getImageData();

		Color unusedColour=png.getUnusedColour();
		int[] intImage=imageData.getRgbInts().clone();
		int[] intReference=imageDataReference.getRgbInts().clone();

		//if(true) {
		createOptimizedImage(intImage,intReference,unusedColour.getColor(),imageData);
		//}else {
		//	this.imagedata=imageData.getImageBytes();
		//}
	}

	private void createOptimizedImage(int[] intImage,int[] intReference,int unusedColour,ImageData imageData) {
		for(int i=0;i<intImage.length;i++) {
			if(intImage[i]==intReference[i] && intImage[++i]==intReference[i]) {
				intImage[i-1]=unusedColour;
				intImage[i++]=unusedColour;
				for(;i<intImage.length;i++) {
					if(intImage[i]==intReference[i]) {
						intImage[i]=unusedColour;
					}else {
						break;
					}
				}
			}
		}

		//System.out.println("originalImagedata: "+intImage.length);
		//System.out.println("\t"+width+" / "+height);
		intImage=cropTransparent(intImage,unusedColour);
		//		intImage=cropPixel(intImage,intReference);
		//System.out.println("croppedImagedata: "+intImage.length);
		//System.out.println("\t"+width+" / "+height);

		if(imageData.getNumberOfColours()>256) {
			this.imagedata=ImageTools.changeIntRGBtoByteRGB(intImage);
		}else if(imageData.isGreyscale()){
			this.imagedata=ImageTools.changeIntGreyscaleToByteGreyscale(intImage);
		}else {
			this.imagedata=imageData.convertToPaletteImage(intImage);
		}

	}

	private int[] cropTransparent(int[] intImage, int transparentColour) {
		int top;
		searchTopBorder:
			for(top=0;top<height;top++) {
				for(int x=0;x<width;x++) {
					if(intImage[top*width+x]!=transparentColour) {
						break searchTopBorder;
					}
				}
			}

		int bottom;
		searchBottomBorder:
			for(bottom=height-1;bottom>=0;bottom--) {
				for(int x=0;x<width;x++) {
					if(intImage[bottom*width+x]!=transparentColour) {
						break searchBottomBorder;
					}
				}
			}

		int left;
		searchLeftBorder:
			for(left=0;left<width;left++) {
				for(int y=top;y<=bottom;y++) {
					int index=y*width+left;
					if(intImage[index]!=transparentColour) {
						break searchLeftBorder;
					}
				}
			}

		int right;
		searchRightBorder:
			for(right=width-1;right>=0;right--) {
				for(int y=top;y<=bottom;y++) {
					int index=y*width+right;
					if(intImage[index]!=transparentColour) {
						break searchRightBorder;
					}
				}
			}

		int widthOriginal=width;
		offsetX=left;
		offsetY=top;
		this.width=right-left+1;
		this.height=bottom-top+1;

		int[] newInts=new int[this.height*this.width];
		int index=0;
		for(int y=top;y<=bottom;y++) {
			for(int x=left;x<=right;x++) {
				newInts[index++]=intImage[y*widthOriginal+x];
			}
		}

		return newInts;
	}

	private int[] cropPixel(int[] intImage, int[] intImageReference) {
		int top;
		searchTopBorder:
			for(top=0;top<height;top++) {
				for(int x=0;x<width;x++) {
					if(intImage[top*width+x]!=intImageReference[top*width+x]) {
						break searchTopBorder;
					}
				}
			}

		int bottom;
		searchBottomBorder:
			for(bottom=height-1;bottom>=0;bottom--) {
				for(int x=0;x<width;x++) {
					if(intImage[bottom*width+x]!=intImageReference[bottom*width+x]) {
						break searchBottomBorder;
					}
				}
			}

		int left;
		searchLeftBorder:
			for(left=0;left<width;left++) {
				for(int y=top;y<=bottom;y++) {
					int index=y*width+left;
					if(intImage[index]!=intImageReference[index]) {
						break searchLeftBorder;
					}
				}
			}

		int right;
		searchRightBorder:
			for(right=width-1;right>=0;right--) {
				for(int y=top;y<=bottom;y++) {
					int index=y*width+right;
					if(intImage[index]!=intImageReference[index]) {
						break searchRightBorder;
					}
				}
			}

		int widthOriginal=width;
		offsetX=left;
		offsetY=top;
		this.width=right-left+1;
		this.height=bottom-top+1;

		int[] newInts=new int[this.height*this.width];
		int index=0;
		for(int y=top;y<=bottom;y++) {
			for(int x=left;x<=right;x++) {
				newInts[index++]=intImage[y*widthOriginal+x];
			}
		}

		return newInts;
	}

	public byte[] getImagedata() {
		return imagedata;
	}

	public int[] getImagedataInt() {
		return intImage;
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
		sb.append("ImagedataOptimizer");
		sb.append("\n\tOffsetX: "+offsetX);
		sb.append("\n\tOffsetY: "+offsetY);
		sb.append("\n\tWidth: "+width);
		sb.append("\n\tHeight: "+height);
		return sb.toString();
	}
}
