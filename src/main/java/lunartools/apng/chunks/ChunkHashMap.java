package lunartools.apng.chunks;

import java.util.HashMap;

/**
 * A Map of all chunks supported by APNG-builder.
 * 
 * @see Chunk_IHDR
 * @see Chunk_tRNS
 * @see Chunk_tEXt
 * @see Chunk_zTXt
 * @see Chunk_PLTE
 * @see Chunk_IDAT
 * @see Chunk_IEND
 * @see Chunk_acTL
 * @see Chunk_fcTL
 * @see Chunk_fdAT
 * @author Thomas Mattel
 */
@SuppressWarnings("rawtypes")
public class ChunkHashMap extends HashMap<String, Class>{

	ChunkHashMap() {
		put(Chunk_IHDR.TYPE,Chunk_IHDR.class);
		put(Chunk_tRNS.TYPE,Chunk_tRNS.class);
		put(Chunk_tEXt.TYPE,Chunk_tEXt.class);
		put(Chunk_zTXt.TYPE,Chunk_zTXt.class);
		put(Chunk_PLTE.TYPE,Chunk_PLTE.class);
		put(Chunk_IDAT.TYPE,Chunk_IDAT.class);
		put(Chunk_IEND.TYPE,Chunk_IEND.class);

		put(Chunk_acTL.TYPE,Chunk_acTL.class);
		put(Chunk_fcTL.TYPE,Chunk_fcTL.class);
		put(Chunk_fdAT.TYPE,Chunk_fdAT.class);
	}

}
