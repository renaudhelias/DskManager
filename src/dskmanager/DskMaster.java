package dskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public LinkedHashMap<Integer, DskSector> findCat(byte[] entriesSector) {
		LinkedHashMap<Integer, DskSector> cats= new LinkedHashMap<Integer, DskSector>();
		int k=2;
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, new Comparator<DskSector>() {
			@Override
			public int compare(DskSector o1, DskSector o2) {
				if (o1.trackC<o2.trackC) {
					return -1;
				}
				if (o1.trackC>o2.trackC) {
					return 1;
				}
				if (o1.sectorIdR<o2.sectorIdR) {
					return -1;
				}
				if (o1.sectorIdR>o2.sectorIdR) {
					return 1;
				}
				return 0;
			}
		});

		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				for (byte b : entriesSector) {
					if (b==k) {
						cats.put((int)b, sector);
						allCats.put((int)b, sector);
					}
				}
				k++;
			}
		}
		return cats;
	}
	
	/**
	 * recherche un cat libre et pas dans C1-C4
	 * @return
	 */
	public int nextFreeCat() {
		// k à 2 car les cats C1(k==0) et C2(k==0) sont figé pour le CAT
		int k=2;
		
		List<DskSector> allCSectors = new ArrayList<DskSector>(allSectors);
		Collections.sort(allCSectors, new Comparator<DskSector>() {
			@Override
			public int compare(DskSector o1, DskSector o2) {
				if (o1.trackC<o2.trackC) {
					return -1;
				}
				if (o1.trackC>o2.trackC) {
					return 1;
				}
				if (o1.sectorIdR<o2.sectorIdR) {
					return -1;
				}
				if (o1.sectorIdR>o2.sectorIdR) {
					return 1;
				}
				return 0;
			}
		});
		
		for (DskSector sector : allCSectors) {
			if (!(sector instanceof DskSectorCatalogs)) {
				if (!allCats.containsKey(k)) {
					allCats.put(k,sector);
					return k;
				}
				k++;
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
