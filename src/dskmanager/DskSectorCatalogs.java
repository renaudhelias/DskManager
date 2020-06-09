package dskmanager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DskSectorCatalogs extends DskSector {
	
	List<DskSectorCatalog> cats = new ArrayList<DskSectorCatalog>();
	public DskSectorCatalogs(int sectorTrack, int sectorId, DskFile dskFile) {
		super(sectorTrack,sectorId, dskFile);
	}

	/**
	 * 
	 * @param fos
	 * @param fileName
	 * @return > zero si pas assez de jocker FIXME
	 * @throws IOException
	 */
	public void scanCatalog(FileChannel channel, String fileName, List<DskSector> listSector) throws IOException {
		// catEntry is not data's target of entry.
		DskSectorCatalog cat = new DskSectorCatalog();
		cat.sectors=listSector;
		cat.filename=fileName;
		if (cat.sectors.size()>0x10) {
			System.out.println("ça ne tient pas dans C1, faudra utiliser C2-C4");
		}
		cat.scan(channel.position(0x200+cats.size()*0x20), fileName);
		cats.add(cat);
		
		
		
//		// à simplifier
//		entrySectors = new byte[0x10];
//		fis.read(entrySectors);
//
//		}
		
	}
//
//	private byte[] newEntrySectors() {
//		return new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
//	}
//	
//	private byte[] computeUsedSector(byte[] usedSectorEntry,byte [] read) {
//		List<Byte> array=new ArrayList<Byte>();
//		for (int i=0;i<0x10;i++) {
//			if (usedSectorEntry[i]!=0) {
//				array.add(usedSectorEntry[i]);
//			}
//			if (read[i]!=0) {
//				array.add(read[i]);
//			}
//		}
//		byte[] arrayb= new byte[array.size()];
//		for (int i=0;i<arrayb.length;i++) {
//			arrayb[i]=array.get(i);
//		}
//		return arrayb;
//	}
	
	
	public String toString() {
		String s="DskSectorCatalogs "+String.format("#%02X", sectorIdR)+" with "+cats.size()+" cats\n";
		for (DskSectorCatalog cat:cats) {
			s+="cat "+cat.toString();
		}
		return s+super.toString();
	}
}
