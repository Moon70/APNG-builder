package lunartools.apng.chunks;

import java.lang.reflect.Constructor;

import lunartools.ByteTools;

/**
 * Creates chunk objects from PNG data.
 * 
 * @author Thomas Mattel
 */
public class ChunkFactory {
	private ChunkHashMap allKnownChunks=new ChunkHashMap();

	/**
	 * Creates a chunk object from the given position of the PNG data.
	 * 
	 * @param baPng The bytearray of the PNG data.
	 * @param offset Offset to the chunk data.
	 * @return The created chunk object.
	 */
	public Chunk createChunk(byte[] baPng, int offset){
		int length=(int)ByteTools.bBytearrayToLongword(baPng,offset);
		String type=getChunkType(baPng,offset+Chunk.LENGTH_SIZEINBYTES);
		@SuppressWarnings("unchecked")
		Class<Chunk> clazz=allKnownChunks.get(type);
		if(clazz!=null) {
			Chunk chunk=null;
			try {
				Constructor<Chunk> constructor=clazz.getDeclaredConstructor(byte[].class,Integer.class,Integer.class);
				chunk=constructor.newInstance(baPng,offset,length);
			} catch (Exception e) {
				throw new RuntimeException("could not create chunk object: "+type,e);
			}
			return chunk;
		}else {
			return new Chunk_OTHER(baPng,offset,length,type);
		}
	}

	private String getChunkType(byte[] baPng, int index) {
		return new String(baPng, index, Chunk.CHUNKTYPE_SIZEINBYTES);
	}

}
