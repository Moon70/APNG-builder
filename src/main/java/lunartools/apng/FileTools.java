package lunartools.apng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileTools{

	/**
	 * Read file into a bytearay.
	 * 
	 * @param file The file to read
	 * @return Content of file as bytearray
	 * @throws IOException
	 */
	public static byte[] readFileAsByteArray(File file) throws IOException{
		InputStream inputStream=null;
		long lenFile=file.length();
		byte[] bytes=new byte[(int)lenFile];
		try{
			inputStream=new FileInputStream(file);
			inputStream.read(bytes);
		}finally{
			if(inputStream!=null){
				inputStream.close();
			}
		}
		return bytes;
	}

	/**
	 * Write (create/overwrite or append) bytearray to file.
	 * 
	 * @param file The file to create/overwrite or append to
	 * @param bytearray The bytearray to write
	 * @param append <code>false</code> to create/overwrite a file, <true> to append to existing file
	 * @throws IOException
	 */
	public static void writeFile(File file,byte[] bytearray,boolean append) throws IOException{
		FileOutputStream fileOutputStream=new FileOutputStream(file.getAbsolutePath(),append);
		try{
			fileOutputStream.write(bytearray);
			fileOutputStream.flush();
		}finally{
			if(fileOutputStream!=null){
				fileOutputStream.close();
			}
		}
	}

}
