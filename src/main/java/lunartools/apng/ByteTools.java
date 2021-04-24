package lunartools.apng;

public class ByteTools {

	public static byte[] longwordToBytearray(int longword) {
		int byte0=(longword>>24)&0xff;
		int byte1=(longword>>16)&0xff;
		int byte2=(longword>>8)&0xff;
		int byte3=longword&0xff;
		return new byte[] {(byte)byte0,(byte)byte1,(byte)byte2,(byte)byte3};
	}
	
	public static byte[] longwordToBytearray(long longword) {
		long byte0=(longword>>24)&0xff;
		long byte1=(longword>>16)&0xff;
		long byte2=(longword>>8)&0xff;
		long byte3=longword&0xff;
		return new byte[] {(byte)byte0,(byte)byte1,(byte)byte2,(byte)byte3};
	}
	
	public static byte[] wordToBytearray(int word) {
		int byte0=(word>>8)&0xff;
		int byte1=word&0xff;
		return new byte[] {(byte)byte0,(byte)byte1};
	}
	
	public static byte[] hexStringToByteArray(String hex) {
		byte[] ba=new byte[hex.length()>>1];
		for(int i=0;i<hex.length();i+=2) {
			int bytevalue=Integer.parseInt(hex.substring(i, i+2), 16);
			ba[i>>1]=(byte)bytevalue;
		}
		return ba;
	}
	
	public static void insertLongword(byte[] bytes,int offset,long longword) {
		byte[] ba=longwordToBytearray(longword);
		for(int i=0;i<4;i++) {
			bytes[offset+i]=ba[i];
		}
	}

	public static long bytearrayToLongword(byte[] bytearray, int offset) {
		long byte0=bytearray[offset];
		long byte1=bytearray[offset+1];
		long byte2=bytearray[offset+2];
		long byte3=bytearray[offset+3];
		if(byte0<0) byte0+=256;
		if(byte1<0) byte1+=256;
		if(byte2<0) byte2+=256;
		if(byte3<0) byte3+=256;
		return (byte0<<24)|(byte1<<16)|(byte2<<8)|byte3;
	}

	public static long bytearrayToWord(byte[] bytearray, int offset) {
		long byte0=bytearray[offset];
		long byte1=bytearray[offset+1];
		if(byte0<0) byte0+=256;
		if(byte1<0) byte1+=256;
		return (byte0<<8)|byte1;
	}

	public static long bytearrayToByte(byte[] bytearray, int offset) {
		long byte0=bytearray[offset];
		if(byte0<0) byte0+=256;
		return byte0;
	}

	public static boolean compareBytes(byte[] bytearray1,byte[] bytearray2,int offsetBytearray2) {
		for(int i=0;i<bytearray1.length;i++) {
			if(bytearray2[offsetBytearray2+i]!=bytearray1[i]) {
				return false;
			}
		}
		return true;
	}

}
