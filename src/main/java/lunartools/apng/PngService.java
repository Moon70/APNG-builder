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
import lunartools.apng.chunks.Chunk_IHDR.ColourType;
import lunartools.apng.chunks.Chunk_PLTE;
import lunartools.apng.chunks.Chunk_tRNS;

public class PngService {
	private static Logger logger = LoggerFactory.getLogger(PngService.class);

	static void createPngViaImageIO(Png png) {
		BufferedImage bufferedImage=png.getImageData().getBufferedImage();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			logger.debug("Creating PNG using ImagerIO");
			ImageIO.write(bufferedImage, "PNG", baos);
		} catch (IOException e) {
			throw new RuntimeException("error creating PNG data",e);
		}
		png.baPng=baos.toByteArray();
	}

	static void createPngViaPngEncoder(Png png) {
		try {
			ImageData imageData=png.getImageData();
			AnimData animData=png.getAnimData();
			int numberOfColours=animData.getNumberOfColours();
			int bitdepth;
			ColourType colourtype;
			Chunk_PLTE chunk_PLTE=null;
			Chunk_tRNS chunk_tRNS=null;
			int maxPaletteColours=png.getFirstPng().getBuilder().getMinimumNumberOfTransparentPixel()==0?256:255;
			if(numberOfColours>maxPaletteColours) {
				bitdepth=8;
				colourtype=ColourType.TRUECOLOUR;
				Color unusedColour=animData.getUnusedColour();
				chunk_tRNS=new Chunk_tRNS(unusedColour.getRed(),unusedColour.getGreen(),unusedColour.getBlue());
			}else if(animData.isGreyscale()) {
				bitdepth=8;
				colourtype=ColourType.GREYSCALE;
				Color unusedColour=animData.getUnusedColour();
				chunk_tRNS=new Chunk_tRNS(unusedColour.getColor());
			}else {
				bitdepth=8;
				colourtype=ColourType.INDEXEDCOLOUR;
				ArrayList<Color> palette=animData.getPalette();
				chunk_PLTE=new Chunk_PLTE(palette);
				int[] alphaPalette=new int[palette.size()];
				for(int i=1;i<alphaPalette.length;i++) {
					alphaPalette[i]=255;
				}
				chunk_tRNS=new Chunk_tRNS(alphaPalette);
			}

			byte[] baImageRaw;
			Chunk_IHDR chunk_IHDR;
			logger.debug("Creating PNG using PngEncoder");
			if(png.getPreviousPng()==null) {
				logger.trace("create primary PNG");
				PngEncoder pngEncoder=new PngEncoder();
				int width=imageData.getWidth();
				int height=imageData.getHeight();
				baImageRaw=pngEncoder.encodePng(imageData.getImageBytes(), width, height,animData.getBytesPerPixel());
				chunk_IHDR=new Chunk_IHDR(width, height, bitdepth, colourtype);
			}else {
				logger.trace("create secondary PNG");
				ImagedataOptimizer imagedataOptimizer=new ImagedataOptimizer();
				imagedataOptimizer.optimizeImage(png);
				//System.out.println("imagedataOptimizer: "+imagedataOptimizer);
				baImageRaw=new PngEncoder().encodePng(imagedataOptimizer.getImagedata(),imagedataOptimizer.getwidth(),imagedataOptimizer.getHeight(),animData.getBytesPerPixel());
				chunk_IHDR=new Chunk_IHDR(imagedataOptimizer.getwidth(), imagedataOptimizer.getHeight(), bitdepth, colourtype);
				png.setOffsets(imagedataOptimizer.getOffsetX(),imagedataOptimizer.getOffsetY());
			}
			png.addChunk(chunk_IHDR);
			if(chunk_PLTE!=null) {
				png.addChunk(chunk_PLTE);
			}
			if(chunk_tRNS!=null) {
				png.addChunk(chunk_tRNS);
			}

			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			Deflater deflater=new Deflater(Deflater.BEST_COMPRESSION,false);
//			deflater.setStrategy(Deflater.HUFFMAN_ONLY);
			deflater.setStrategy(Deflater.FILTERED);
//			deflater.setStrategy(Deflater.DEFAULT_STRATEGY);
//			deflater.setStrategy(Deflater.NO_FLUSH);
			DeflaterOutputStream deflaterOutputStream=new DeflaterOutputStream(baos,deflater);
			deflaterOutputStream.write(baImageRaw);
			deflaterOutputStream.finish();
			deflaterOutputStream.flush();
			deflaterOutputStream.close();
			byte[] imagedataCompressed=baos.toByteArray();

			final int chunkdatasize=png.getBuilder().getImageDataChunkSize();
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
