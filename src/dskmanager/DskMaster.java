package dskmanager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * un DskManager, personnel à un DskFile
 * @author Joe
 *
 */
public class DskMaster {

	
	
	int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
	int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
	// dictionary
	byte[]usedSectorEntry={};
	// contient aussi C1 C2 C3 C4
	List<DskSector> allSectors = new ArrayList<DskSector>();
	LinkedHashMap<Integer, DskSector> allCats= new LinkedHashMap<Integer, DskSector>();
	
	public DskMaster() {
	}
	
	DskSector find(DskTrack track, int sectorId) {
		for (DskSector sector:track.sectors) {
			if (sector.sectorIdR==sectorId) {
				return sector;
			}
		}
		return null;
	}
	
	/**
	 * Repli allCats au passage
	 * @param entriesSector
	 * @param sectors
	 * @return
	 */
	public LinkedHashMap<Integer, DskSector> findCat(byte[] entriesSector, List<DskSector> sectors) {
		LinkedHashMap<Integer, DskSector> cats= new LinkedHashMap<Integer, DskSector>();
		for (int i=0+2;i<sectors.size()+2;i++) {
			for (byte b : entriesSector) {
				if (b==i) {
					cats.put((int)b, sectors.get(i-2));
					allCats.put((int)b, sectors.get(i-2));
				}
			}
		}
		return cats;
	}
	
	/**
	 * recherche un cat libre et pas dans C1-C4
	 * @return
	 */
	public int nextFreeCat() {
		// k à 0x00 car le second track a 0x00
		for (int k=4;k<allSectors.size();k++) {
			if (!allCats.containsKey(k)) {
				allCats.put(k,allSectors.get(k));
				return k;
			}
		}
		return 2;
	}
	
	
	public String arrayToString(byte[] bufferHeader) {
		
		StringBuilder sb = new StringBuilder();
		for (byte b: bufferHeader) {
			char ch = (char)b;
			sb.append(ch);
		}

		return sb.toString();
	}

}
