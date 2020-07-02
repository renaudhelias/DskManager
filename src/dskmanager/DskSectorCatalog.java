package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	List<Integer> catsId= new ArrayList<Integer>();
	List<DskSector> catsSector= new ArrayList<DskSector>();
	int sectorIncrement;
	int sectorLength;
	byte [] entriesSector = new byte[0x10];
	boolean leftEntries=true;
	
	public DskSectorCatalog(DskMaster master) {
		this.master = master;
	}
	public boolean scan(ByteArrayInputStream bis) throws IOException {
		jocker = bis.read();
		if ((jocker & 0xFF) == 0xE5) {
			// not a user
			bis.skip(0x20-1);
			return false;
		}
		byte[] filename = new byte[11];
		bis.read(filename);
		this.filename = master.arrayToString(filename);
		
		sectorIncrement=bis.read();
		bis.read();bis.read();
		sectorLength=bis.read();
		
		entriesSector = new byte[0x10];
		bis.read(entriesSector);
		
		this.catsId=master.findCatsId(entriesSector);
		this.catsSector=master.findCatsSector(entriesSector);
		return true;
	}
	
	public void scan(ByteArrayOutputStream bos, Map<String, DskSectorCatalog> previousCats) throws IOException {
		if (jocker == 0xE5) {
			// fill 0xE5
			for (int i = 0;i<0x20;i++) {
				bos.write(0xE5);
			}
			return;
		}
		bos.write(new byte[]{(byte)jocker});
		byte [] entryFileName = master.stringToArray(master.realname2cpcname(filename));
		bos.write(entryFileName);
		bos.write(new byte[]{(byte)sectorIncrement});
		bos.write(new byte[]{0,0});
		bos.write(new byte[]{(byte)sectorLength});
		
		int nbTrou=0;
		if (master.type==DskType.DOSD40 || master.type==DskType.SDOS) {
			if (previousCats != null && previousCats.get(filename)!=null) {
				leftEntries = !previousCats.get(filename).leftEntries;
				previousCats.put(filename, this);
			} else {
				leftEntries = true;
				previousCats.put(filename, this);
			}
			if (!leftEntries) {
				for (int k=0;k<8;k++) {
					bos.write(0);
					nbTrou++;
				}
			}
		}
		for (Integer cat:catsId) {
			if (master.type==DskType.DOSD40 || master.type==DskType.SDOS) {
				// first part of entriesSector or else last part of entrieSector
				// 8 catIds by here only.
				bos.write((byte) cat.intValue());
			} else if (master.type==DskType.DOSD2 || master.type==DskType.DOSD10 || master.type==DskType.DOSD20 || master.type==DskType.VORTEX) {
				byte k1=(byte)(cat & 0xff);
				byte k2=(byte)((cat & 0xff00) >> 8);
				bos.write(k1);
				bos.write(k2);
				nbTrou++;
			} else if (master.type==DskType.PARADOS41 || master.type==DskType.SS40 || master.type==DskType.SYSTEM) {
				bos.write((byte) cat.intValue());
			}
		}
		for (int j=0;j<0x10-(catsId.size()+nbTrou);j++) {
			bos.write(new byte[]{0});
		}
		
	}

	public String toString() {
		String s="DskSectorCatalog\n"+catsId.size()+" sectors references\n";
		for (Integer cat:catsId) {
			s+="cat:"+String.format("#%02X", cat)+"\n";
		}
		return s;
	}
}
