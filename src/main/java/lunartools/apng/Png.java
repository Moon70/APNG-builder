package lunartools.apng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import lunartools.apng.chunks.Chunk;
import lunartools.apng.chunks.ChunkFactory;
import lunartools.apng.chunks.Chunk_IDAT;
import lunartools.apng.chunks.Chunk_IEND;
import lunartools.apng.chunks.Chunk_IHDR;
import lunartools.apng.chunks.Chunk_acTL;
import lunartools.apng.chunks.Chunk_fcTL;
import lunartools.apng.chunks.Chunk_fdAT;

/*
 * PNG reference: https://www.w3.org/TR/PNG
 * APNG reference: https://wiki.mozilla.org/APNG_Specification
 * https://docs.fileformat.com/image/apng/
 */
/**
 * Represents a PNG (Portable Network Graphics)
 * <br>A Png instance holds the binary data of a PNG file.
 * <br>More Png instances can be added to an instance, to create an animated PNG file.
 * <br><br><b>This is an alpha version.
 * <br>It does not yet reencode the image data, but only combine existing image data and insert APNG chunks.
 * </b>
 * 
 * @author Thomas Mattel
 */
public class Png {
	private static final byte[] PNG_SIGNATURE=new byte[] {(byte)0x89,(byte)0x50,(byte)0x4e,(byte)0x47,(byte)0x0d,(byte)0x0a,(byte)0x1a,(byte)0x0a};
	private byte[] baPng;
	private ArrayList<Chunk> chunklist;
	private ArrayList<Png> listPng=new ArrayList<Png>();
	private Chunk_IHDR chunk_IHDR;
	private int delay;
	private int offsetX;
	private int offsetY;

	Png() {}

	Png(byte[] pngAsBytearray) {
		this.baPng=pngAsBytearray;
		if(!isPngFile(baPng)){
			throw new RuntimeException("Not a PNG");
		}
	}

	void parsePng(){
		try {
			chunklist=new ArrayList<Chunk>();

			int index=PNG_SIGNATURE.length;
			ChunkFactory chunkFactory=new ChunkFactory();

			Chunk chunk;
			do {
				chunk=chunkFactory.createChunk(baPng,index);
				chunklist.add(chunk);
				if(chunk instanceof Chunk_IHDR) {
					this.chunk_IHDR=(Chunk_IHDR)chunk;
				}
				index+=chunk.getChunkLength();
			}while(!(chunk instanceof Chunk_IEND));
		} catch (Exception e) {
			throw new RuntimeException("exception while parsing PNG file",e);
		}
	}

	byte[] getImagedata() {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			for(int i=0;i<chunklist.size();i++) {
				if(chunklist.get(i) instanceof Chunk_IDAT) {
					Chunk_IDAT chunk=(Chunk_IDAT)chunklist.get(i);
					baos.write(chunk.getChunkData());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Error reading imagedata",e);
		}
		return baos.toByteArray();
	}

	private byte[] getDecodedImagedata() {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			Inflater inflater=new Inflater();
			InflaterOutputStream inflaterOutputStream=new InflaterOutputStream(baos,inflater);
			inflaterOutputStream.write(getImagedata());
			inflaterOutputStream.finish();
			inflaterOutputStream.flush();
			inflaterOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException("Error reading imagedata",e);
		}
		return baos.toByteArray();
	}

	public void addPng(Png png) {
		listPng.add(png);
	}

	public void savePng(File file) throws FileNotFoundException, IOException {
		try(FileOutputStream fos=new FileOutputStream(file)){
			writePngToStream(fos);
		}
	}

	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		writePngToStream(baos);
		return baos.toByteArray();
	}

	private void writePngToStream(OutputStream outputStream) throws IOException {
		ArrayList<Chunk> chunklistWork=chunklist;
		if(listPng.size()>0) {
			chunklistWork=(ArrayList<Chunk>)chunklist.clone();
			addApngChunks(chunklistWork);
		}
		outputStream.write(PNG_SIGNATURE);
		for(int i=0;i<chunklistWork.size();i++) {
			Chunk chunk=chunklistWork.get(i);
			outputStream.write(chunk.toByteArray());
		}
	}

	private void addApngChunks(ArrayList<Chunk> chunklist) {
		int fcTL_sequenceNumber=0;
		Chunk chunk_acTL=new Chunk_acTL(listPng.size()+1,0);
		Chunk chunk_fcTL=new Chunk_fcTL(fcTL_sequenceNumber,getWidth(),getHeight(),0,0,delay,1000,Chunk_fcTL.APNG_DISPOSE_OP_NONE,Chunk_fcTL.APNG_BLEND_OP_SOURCE);

		for(int i=0;i<chunklist.size();i++) {
			if(chunklist.get(i) instanceof Chunk_IDAT) {
				chunklist.add(i, chunk_acTL);
				chunklist.add(i+1, chunk_fcTL);
				break;
			}
		}

		int index_IEND;
		for(index_IEND=0;index_IEND<chunklist.size();index_IEND++) {
			if(chunklist.get(index_IEND) instanceof Chunk_IEND) {
				break;
			}
		}

		for(int i=0;i<listPng.size();i++) {
			Png png=listPng.get(i);
			ArrayList<Chunk_IDAT> arraylistIdatChunks=png.getAllIdatChunks();
			chunk_fcTL=new Chunk_fcTL(++fcTL_sequenceNumber,png.getWidth(),png.getHeight(),png.offsetX,png.offsetY,png.getDelay(),1000,Chunk_fcTL.APNG_DISPOSE_OP_NONE,Chunk_fcTL.APNG_BLEND_OP_SOURCE);
			chunklist.add(index_IEND++, chunk_fcTL);
			for(int x=0;x<arraylistIdatChunks.size();x++) {
				Chunk_IDAT chunk_IDAT=arraylistIdatChunks.get(x);
				Chunk chunk_fdAT=new Chunk_fdAT(++fcTL_sequenceNumber,chunk_IDAT);
				chunklist.add(index_IEND++, chunk_fdAT);
			}
		}

	}

	private ArrayList<Chunk_IDAT> getAllIdatChunks(){
		ArrayList<Chunk_IDAT> arraylistIdatChunks=new ArrayList<Chunk_IDAT>();
		for(int i=0;i<chunklist.size();i++) {
			if(chunklist.get(i) instanceof Chunk_IDAT) {
				arraylistIdatChunks.add((Chunk_IDAT)chunklist.get(i));
			}
		}
		return arraylistIdatChunks;
	}

	public int getWidth() {
		if(chunk_IHDR==null) {
			throw new RuntimeException("no png parsed yet");
		}
		return chunk_IHDR.getWidth();
	}

	public int getHeight() {
		if(chunk_IHDR==null) {
			throw new RuntimeException("no png parsed yet");
		}
		return chunk_IHDR.getHeight();
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffset(int offsetX, int offsetY) {
		this.offsetX=offsetX;
		this.offsetY=offsetY;
	}

	public void setDelay(int delay) {
		this.delay=delay;
	}

	private int getDelay() {
		return delay;
	}

	private static boolean isPngFile(byte[] baPng) {
		return compareBytes(baPng, 0, PNG_SIGNATURE);
	}

	private static boolean compareBytes(byte[] baPng,int index, byte[] compare) {
		for(int i=0;i<compare.length;i++) {
			if(baPng[index+i]!=compare[i]) {
				return false;
			}
		}
		return true;
	}

	public void addChunk(Chunk chunk) {
		if(chunklist==null) {
			chunklist=new ArrayList<Chunk>();
		}
		chunklist.add(chunk);
		if(chunk instanceof Chunk_IHDR) {
			chunk_IHDR=(Chunk_IHDR)chunk;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<chunklist.size();i++) {
			sb.append("chunk "+i+": ");
			sb.append(chunklist.get(i).toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	void printIdatDetails() {
		getImagedata();
	}

}
