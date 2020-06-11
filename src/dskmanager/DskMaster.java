package dskmanager;

import java.util.ArrayList;
import java.util.HashMap;
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
	private DskFile dskFile;
	
	public DskMaster(DskFile dskFile) {
		this.dskFile = dskFile;
	}
	
	HashMap<Integer, DskSector> catSectors = new HashMap<Integer, DskSector>();
	List<DskSector> catSectorsReferenced = new ArrayList<DskSector>();
	public void generateCatSectors() {
		int catRef=0x02;
		for (DskTrack track:dskFile.tracks) {
			for (DskSector sector : track.sectors) {
				if (sector instanceof DskSectorCatalogs) {
					for (DskSectorCatalog cat:((DskSectorCatalogs)sector).cats) {
						catSectorsReferenced.addAll(cat.sectors);
					}
				} else {
					sector.cat=catRef;
					catSectors.put(catRef, sector);
					catRef++;
				}
			}
		}
	}
	public DskSector nextFreeSector() {
		for (DskSector cat : catSectors.values()) {
			if (!catSectorsReferenced.contains(cat)) {
				catSectorsReferenced.add(cat);
				return cat; 
			}
		}
		return null;
	}
	

	DskSector find(DskTrack track, int sectorId) {
		for (DskSector sector:track.sectors) {
			if (sector.sectorIdR==sectorId) {
				return sector;
			}
		}
		return null;
	}
	
	DskSector find(int cat) {
		for (DskTrack track:dskFile.tracks) {
			for (DskSector sector:track.sectors) {
				if (sector.cat==cat) {
					return sector;
				}
			}
		}
		return null;
	}
	public List<DskSector> explose(byte[] entriesSector, List<DskSector> sectors) {
		List<DskSector> sectorsData =new ArrayList<DskSector>();
		for (int cat:entriesSector) {
			sectorsData.add(find(cat));
		}
		return sectorsData;
	}
	
}
