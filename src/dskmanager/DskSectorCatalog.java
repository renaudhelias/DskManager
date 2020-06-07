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
	// FIXME : plus tard ça.
	//	List<String> catalog = new ArrayList<String>();
	
	public DskSectorCatalog(int sectorTrack, int sectorId, DskFile dskFile) {
		super(sectorTrack,sectorId, dskFile);
	}
//	public int addFilename(String filename) {
//		this.catalog.add(filename);
//		return catalog.size()-1;
//	}
	
	//**
	
	
	/**
	 * 
	 * @param fos
	 * @param fileName
	 * @return > zero si pas assez de jocker FIXME
	 * @throws IOException
	 */
	public void scan(FileChannel channel, String fileName, List<DskSector> listSector) throws IOException {

		// à simplifier
		
		
		channel.position(0x200); // here C1 of track 1
		ByteBuffer jocker=ByteBuffer.allocate(1);
		for (int i =0;i<0x10;i++) {
			channel.position(0x200+i*0x20);
			channel.read(jocker);
			if (jocker.get()!=0x00) {
				channel.write(ByteBuffer.wrap(new byte[]{0x00}));//jocker
				byte [] entryFileName = realname2cpcname(fileName).getBytes();
				channel.write(ByteBuffer.wrap(entryFileName));
				channel.write(ByteBuffer.wrap(new byte[]{(byte)i}));
				channel.write(ByteBuffer.wrap(new byte[]{0,0}));
				for (DskSector sector:listSector) {
					channel.write(ByteBuffer.wrap(new byte[]{(byte)sector.cat}));
				}
				for (int j=0;j<0x10-listSector.size();j++) {
					channel.write(ByteBuffer.wrap(new byte[]{0}));					
				}
				
			}
		}
		
	}

	private byte[] newEntrySectors() {
		return new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
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
