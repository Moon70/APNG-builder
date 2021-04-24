package lunartools.apng.chunks;

import java.util.HashMap;

@SuppressWarnings("rawtypes")
public class ChunkHashMap extends HashMap<String, Class>{

	ChunkHashMap() {
		put(Chunk_IHDR.TYPE,Chunk_IHDR.class);
		put(Chunk_tRNS.TYPE,Chunk_tRNS.class);
		put(Chunk_tEXt.TYPE,Chunk_tEXt.class);
		put(Chunk_zEXt.TYPE,Chunk_zEXt.class);
		put(Chunk_PLTE.TYPE,Chunk_PLTE.class);
		put(Chunk_IDAT.TYPE,Chunk_IDAT.class);
		put(Chunk_IEND.TYPE,Chunk_IEND.class);

		put(Chunk_acTL.TYPE,Chunk_acTL.class);
		put(Chunk_fcTL.TYPE,Chunk_fcTL.class);
		put(Chunk_fdAT.TYPE,Chunk_fdAT.class);
	}

}
