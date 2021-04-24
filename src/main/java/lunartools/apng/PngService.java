package lunartools.apng;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lunartools.apng.chunks.Chunk_IDAT;
import lunartools.apng.chunks.Chunk_IEND;
import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.apng.chunks.Chunk_PLTE;
import lunartools.apng.chunks.Chunk_tRNS;
import lunartools.pngidatcodec.PngEncoder;

public class PngService {
	private static Logger logger = LoggerFactory.getLogger(PngService.class);

	static void createPngViaImageIO(Png png) {
		BufferedImage bufferedImage=png.getImageData().getBufferedImage();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, "PNG", baos);
		} catch (IOException e) {
			throw new RuntimeException("error creating PNG data",e);
		}
		png.baPng=baos.toByteArray();
		png.parsePng();
	}

	static void createPngViaPngEncoder(Png png) {
		try {
			ImageData imageData=png.getImageData();
			int numberOfColours=imageData.getNumberOfColours();
			int bitdepth;
			int colourtype;
			Chunk_PLTE chunk_PLTE=null;
			Chunk_tRNS chunk_tRNS=null;
			if(numberOfColours>255) {
				bitdepth=8;
				colourtype=Chunk_IHDR.COLOURTYPE_TRUECOLOUR;
				Color unusedColour=png.getFirstPng().getUnusedColour();
				chunk_tRNS=new Chunk_tRNS(unusedColour.getRed(),unusedColour.getGreen(),unusedColour.getBlue());

			}else if(imageData.isGreyscale()) {
				bitdepth=8;
				colourtype=Chunk_IHDR.COLOURTYPE_GREYSCALE;
				Color unusedColour=png.getFirstPng().getUnusedColour();
				chunk_tRNS=new Chunk_tRNS(unusedColour.getColor());
			}else {
				bitdepth=8;
				colourtype=Chunk_IHDR.COLOURTYPE_INDEXEDCOLOUR;
				ArrayList<Color> palette=imageData.getPalette();
				chunk_PLTE=new Chunk_PLTE(palette);
				int[] alphaPalette=new int[palette.size()];
				for(int i=1;i<alphaPalette.length;i++) {
					alphaPalette[i]=255;
				}
				chunk_tRNS=new Chunk_tRNS(alphaPalette);
			}

			byte[] baImageRaw;
			Chunk_IHDR chunk_IHDR;
			if(png.getPreviousPng()==null) {
				PngEncoder pngEncoder=new PngEncoder();
				int width=imageData.getWidth();
				int height=imageData.getHeight();
				baImageRaw=pngEncoder.encodePng(imageData.getImageBytes(), width, height,imageData.getBytesPerPixel(),imageData.isGreyscale());
				chunk_IHDR=new Chunk_IHDR(width, height, bitdepth, colourtype);
				logger.trace("IHDR 1: {}",chunk_IHDR);
			}else {
				ImagedataOptimizer imagedataOptimizer=new ImagedataOptimizer();
				imagedataOptimizer.optimizeImage(png);
				//System.out.println("imagedataOptimizer: "+imagedataOptimizer);

				baImageRaw=new PngEncoder().encodePng(imagedataOptimizer.getImagedata(),imagedataOptimizer.getwidth(),imagedataOptimizer.getHeight(),imageData.getBytesPerPixel(),imageData.isGreyscale());
				chunk_IHDR=new Chunk_IHDR(imagedataOptimizer.getwidth(), imagedataOptimizer.getHeight(), bitdepth, colourtype);
				logger.trace("IHDR +: {}",chunk_IHDR);
				png.setOffset(imagedataOptimizer.getOffsetX(),imagedataOptimizer.getOffsetY());
				logger.trace("offset {} / {}",imagedataOptimizer.getOffsetX(),imagedataOptimizer.getOffsetY());
			}
			png.addChunk(chunk_IHDR);

			if(chunk_PLTE!=null) {
				logger.trace("adding palette chunk: {}",chunk_PLTE);
				png.addChunk(chunk_PLTE);
			}

			if(chunk_tRNS!=null) {
				logger.trace("adding tRNS chunk: {}",chunk_tRNS);
				png.addChunk(chunk_tRNS);
			}

			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			Deflater deflater=new Deflater(Deflater.BEST_COMPRESSION);
			DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(baos,deflater);
			deflaterOutputStream.write(baImageRaw);
			deflaterOutputStream.finish();
			deflaterOutputStream.flush();
			deflaterOutputStream.close();
			byte[] imagedataCompressed=baos.toByteArray();

			final int chunkdatasize=65536;
			for(int i=0;i<imagedataCompressed.length;i+=chunkdatasize) {
				int len=i+chunkdatasize;
				if(len>imagedataCompressed.length) {
					len=imagedataCompressed.length;
				}
				Chunk_IDAT chunk_IDAT=new Chunk_IDAT(Arrays.copyOfRange(imagedataCompressed, i, len));
				png.addChunk(chunk_IDAT);
			}
			Chunk_IEND chunk_IEND=new Chunk_IEND();
			png.addChunk(chunk_IEND);
		} catch (Exception e) {
			throw new RuntimeException("error encoding PNG",e);
		}
	}

}
