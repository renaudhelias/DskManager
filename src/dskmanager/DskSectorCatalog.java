package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * One per filename
 * + list of sector(track0 C6...) by entry (0x02 0x03...)
 * @author Joe
 *
 */
public class DskSectorCatalog {
	private DskMaster master;

	int jocker;
	String filename;
//	LinkedHashMap<Integer,DskSector> catSectors = new LinkedHashMap<Integer,DskSector>();
	List<Integer> catsId= new ArrayList<Integer>();
	List<DskSector> catsSector= new ArrayList<DskSector>();
	int sectorIncrement;
	int sectorLength;
	
	public DskSectorCatalog(DskMaster master) {
		this.master = master;
	}
	public boolean scan(ByteArrayInputStream bis) throws IOException {
		jocker = bis.read();
		if (jocker !=0) return false;
		byte[] filename = new byte[11];
		bis.read(filename);
		this.filename = master.arrayToString(filename);
		
		sectorIncrement=bis.read();
		bis.read();bis.read();
		sectorLength=bis.read();
		
		byte[] entriesSector = new byte[0x10];
		bis.read(entriesSector);
		
//		this.catSectors=master.findCat(entriesSector);
		this.catsId=master.findCatsId(entriesSector);
		this.catsSector=master.findCatsSector(entriesSector);
		return true;
	}
	
	
	public void scan(ByteArrayOutputStream bos) throws IOException {
		bos.write(new byte[]{(byte)jocker});
		byte [] entryFileName = realname2cpcname(filename).getBytes();
		bos.write(entryFileName);
		bos.write(new byte[]{(byte)sectorIncrement});
		bos.write(new byte[]{0,0});
		bos.write(new byte[]{(byte)sectorLength});
		
		for (Integer cat:catsId) {
			bos.write(new byte[]{(byte)cat.intValue()});
		}
		for (int j=0;j<0x10-catsId.size();j++) {
			bos.write(new byte[]{0});					
		}
		
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

	public String toString() {
		String s="DskSectorCatalog\n"+catsId.size()+" sectors references\n";
		for (Integer cat:catsId) {
			s+="cat:"+String.format("#%02X", cat)+"\n";
		}
		return s;
	}
}
