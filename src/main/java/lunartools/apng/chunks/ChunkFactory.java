package lunartools.apng.chunks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import lunartools.apng.ByteTools;

public class ChunkFactory {
	private ChunkHashMap allKnownChunks=new ChunkHashMap();
	
	public Chunk createChunk(byte[] baPng, int index) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		int length=(int)ByteTools.bytearrayToLongword(baPng,index);
		String type=getChunkType(baPng,index+Chunk.LENGTH_SIZEINBYTES);
		Class<Chunk> clazz=allKnownChunks.get(type);
		if(clazz!=null) {
			Constructor<Chunk> constructor=clazz.getDeclaredConstructor(byte[].class,Integer.class,Integer.class);
			return constructor.newInstance(baPng,index,length);
		}else {
			return new Chunk_OTHER(baPng,index,length,type);
		}
	}

	private String getChunkType(byte[] baPng, int index) {
		return new String(baPng, index, 4);
	}

}
