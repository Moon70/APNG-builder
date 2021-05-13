package lunartools.apng;

/**
 * Optimizes the image to reduce filesize.
 * <li>replacing pixel, that have not changed, with transparent pixel
 * <li>cropping by removing top/bottom/left/right are that has not changed, compared to the previous image
 * 
 * @author Thomas Mattel
 */
public class ImagedataOptimizer {
	private int[] intImage;
	private byte[] imagedata;
	private int offsetX;
	private int offsetY;
	private int width;
	private int height;

	void optimizeImage(Png png) {
		this.offsetX=0;
		this.offsetY=0;

		ImageData imageData=png.getImageData();
		AnimData animData=png.getAnimData();
		this.width=imageData.getWidth();
		this.height=imageData.getHeight();

		Png pngPrevious=png.getPreviousPng();
		ImageData imageDataPrevious=pngPrevious.getImageData();

		Color transparentColour=animData.getUnusedColour();
		int[] intImage=imageData.getRgbInts().clone();
		int[] intReference=imageDataPrevious.getRgbInts().clone();

		int minimumNumberOfTransparentPixel=png.getBuilder().getMinimumNumberOfTransparentPixel();
		if(minimumNumberOfTransparentPixel>0){
//			replaceUnchangedPixelWithTransparentPixel(intImage,intReference,transparentColour.getColor());
			replaceUnchangedPixelWithTransparentPixelNew(intImage,intReference,transparentColour.getColor(),minimumNumberOfTransparentPixel);
			intImage=cropTransparentPixel(intImage,transparentColour.getColor());
		}else {
			intImage=cropUnchangedPixel(intImage,intReference);
		}
		
		if(animData.getNumberOfColours()>256) {
			this.imagedata=ImageTools.changeIntRGBtoByteRGB(intImage);
		}else if(animData.isGreyscale()){
			this.imagedata=ImageTools.changeIntGreyscaleToByteGreyscale(intImage);
		}else {
			this.imagedata=imageData.convertToPaletteImage(intImage);
		}
	}

	private void replaceUnchangedPixelWithTransparentPixel(int[] intImage,int[] intReference,int transparentPixel) {
		for(int i=0;i<intImage.length-5;i++) {
			if(intImage[i]==intReference[i] &&
//					intImage[++i]==intReference[i] && 
					intImage[++i]==intReference[i] && 
					intImage[++i]==intReference[i] && 
					intImage[++i]==intReference[i] && 
					intImage[++i]==intReference[i]) {
//				intImage[i-5]=transparentPixel;
				intImage[i-4]=transparentPixel;
				intImage[i-3]=transparentPixel;
				intImage[i-2]=transparentPixel;
				intImage[i-1]=transparentPixel;
				intImage[i++]=transparentPixel;
				for(;i<intImage.length;i++) {
					if(intImage[i]==intReference[i]) {
						intImage[i]=transparentPixel;
					}else {
						break;
					}
				}
			}
		}
	}
	
	private void replaceUnchangedPixelWithTransparentPixelNew(int[] intImage,int[] intReference,int transparentPixel, int min) {
		pixelcompareloop:
		for(int i=0;i<intImage.length-min;i++) {
			int s=i+min;
			for(;i<s;i++) {
				if(intImage[i]!=intReference[i]) {
					continue pixelcompareloop;
				}
			}
			for(i-=min;i<s;i++) {
				intImage[i]=transparentPixel;
			}
			for(;i<intImage.length;i++) {
				if(intImage[i]==intReference[i]) {
					intImage[i]=transparentPixel;
				}else {
					break;
				}
			}
		}
	}
	
	private int[] cropTransparentPixel(int[] intImage, int transparentColour) {
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

	private int[] cropUnchangedPixel(int[] intImage, int[] intImageReference) {
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

	byte[] getImagedata() {
		return imagedata;
	}

	int[] getImagedataInt() {
		return intImage;
	}

	int getwidth() {
		return width;
	}

	int getHeight() {
		return height;
	}

	int getOffsetX() {
		return offsetX;
	}

	int getOffsetY() {
		return offsetY;
	}

	@Override
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
