package dskmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DskSectorCatalog extends DskSector {
	List<String> catalog = new ArrayList<String>();
	public int addFilename(String filename) {
		this.catalog.add(filename);
		return catalog.size()-1;
	}
	public void scan(FileInputStream fis, int noFileEntry) throws IOException {
		fis.read(catalog.get(noFileEntry).getBytes());
		fis.read();// nbEntry
		fis.read();fis.read();
		byte entryFileSize;
		entryFileSize=(byte) fis.read();
		byte [] entrySectors = new byte[0x10];
		fis.read(entrySectors);
		byte [] usedSectorEntry=new byte[]{};
		usedSectorEntry=computeUsedSector(usedSectorEntry,entrySectors);
		fis.close();
	}
	
	public void scan(FileOutputStream fos,int noFileEntry) {
		
		FileChannel channel = fos.getChannel();
//		channel.position(0x200);
//		for (int i=0;i<nbEntry;i++) {
//			channel.write(ByteBuffer.wrap(new byte[]{0x00}));//jocker
//			entryFileName=realname2cpcname(fileName).getBytes();
//			channel.write(ByteBuffer.wrap(entryFileName));
//			channel.write(ByteBuffer.wrap(new byte[]{(byte)i}));
//			channel.write(ByteBuffer.wrap(new byte[]{0,0}));
//			if (i==nbEntry-1) {
//				channel.write(ByteBuffer.wrap(new byte[]{(byte) (nbEntryLast/2)})); // entrySize
//			} else {
//				channel.write(ByteBuffer.wrap(new byte[]{(byte) (16*1024/2)})); // entrySize
//			}
//			entrySectors = newEntrySectors();
//			channel.write(ByteBuffer.wrap(entrySectors));
//		}
	}

	public static String realname2cpcname(String realname) {
    	String cpcname = realname.toUpperCase();
    	if (cpcname.contains(".")) {
            int point = cpcname.indexOf(".");
            String filename = cpcname.substring(0, point);
            filename = filename + "        ";
            filename = filename.substring(0, 8);
            String extension = cpcname.substring(point + 1,
                    cpcname.length());
            extension = extension + "   ";
            extension = extension.substring(0, 3);

            cpcname = filename + extension;
        } else {
            cpcname = cpcname + "        " + "   ";
            cpcname = cpcname.substring(0, 8 + 3);
        }
    	return cpcname;
    }

	private byte[] computeUsedSector(byte[] usedSectorEntry,byte [] read) {
		List<Byte> array=new ArrayList<Byte>();
		for (int i=0;i<0x10;i++) {
			if (usedSectorEntry[i]!=0) {
				array.add(usedSectorEntry[i]);
			}
			if (read[i]!=0) {
				array.add(read[i]);
			}
		}
		byte[] arrayb= new byte[array.size()];
		for (int i=0;i<arrayb.length;i++) {
			arrayb[i]=array.get(i);
		}
		return arrayb;
	}
}
