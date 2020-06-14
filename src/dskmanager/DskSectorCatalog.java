package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
	LinkedHashMap<Integer,DskSector> catSectors = new LinkedHashMap<Integer,DskSector>();
	int sectorOffset;
	
	public DskSectorCatalog(DskMaster master) {
		this.master = master;
	}
	public void scan(ByteArrayInputStream bis) throws IOException {
		List<DskSector> sectors = master.allSectors;
		jocker = bis.read();
		byte[] filename = new byte[11];
		bis.read(filename);
		this.filename = String.valueOf(filename);
		
		bis.read();bis.read();bis.read();bis.read();
		
		byte[] entriesSector = new byte[0x10];
		bis.read(entriesSector);
		
		this.catSectors=master.findCat(entriesSector,sectors);
	}
	
	
	public void scan(RandomAccessFile fos, String filename) throws IOException {
		fos.write(new byte[]{(byte)jocker});
		byte [] entryFileName = realname2cpcname(filename).getBytes();
		fos.write(entryFileName);
		fos.write(new byte[]{(byte)sectorOffset});
		fos.write(new byte[]{0,0});
		fos.write(new byte[]{(byte)sectorOffset});
		
		for (Integer cat:catSectors.keySet()) {
			fos.write(new byte[]{(byte)cat.intValue()});
		}
		for (int j=0;j<0x10-catSectors.size();j++) {
			fos.write(new byte[]{0});					
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
		String s="DskSectorCatalog\n"+catSectors.size()+" sectors references\n";
		for (Integer cat:catSectors.keySet()) {
			DskSector sector=catSectors.get(cat);
			s+="track "+sector.trackC+" head "+sector.sideH+" cat:"+String.format("#%02X", cat)+" id:"+String.format("#%02X", sector.sectorIdR)+" DATA size "+sector.data.length+"\n";
		}
		return s;
	}
}
