package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * le cat vise des paquets de 1024
 * le 02++ vise donc des paquets de 1024
 * Mais les paquet de 512 sont répartie sur des secteur 512 C1 C2 etc.
 * Problème de mapping sectorID vs catID
 * 
 * Il faut donc garder le granulé sectorID car il est plus précis (sans perte d'info)
 * 
 * J'ai fait l'inverse avec DoubleInteger-catID, ça a cassé aux tests direct.
 * 
 * Donc on a :
 * 
 * catID=>SectorID,SectorID
 * 
 * exact.
 * @author Joe
 *
 */
public class DskSectorCatalogs extends DskSector {
	
	List<DskSectorCatalog> cats = new ArrayList<DskSectorCatalog>();
//	public DskSectorCatalogs(DskMaster master,int track) {
//		super(master,track);
//	}

	public DskSectorCatalogs(DskSector sector) {
		super(sector);
	}

	public void scanCatalog() throws IOException {
		// fill data from cats
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (DskSectorCatalog cat:cats) {
			cat.scan(baos);
		}
		for (int i=cats.size()*0x20;i<master.sectorSizes[sectorSizeN];i++) {
			baos.write(0xE5);
		}
		data = baos.toByteArray();
		baos.close();
	}

	
	public String toString() {
		String s="DskSectorCatalogs "+String.format("#%02X", sectorIdR)+" with "+cats.size()+" cats\n";
		for (DskSectorCatalog cat:cats) {
			s+="cat "+cat.toString();
		}
		return s+super.toString();
	}

	/**
	 * Un scan catalog courageux, lors d'un loadDsk ou newDsk
	 * @throws IOException
	 */
	public void scanCatalogFromData() throws IOException {
		// fill cats from data
		ByteArrayInputStream bis=new ByteArrayInputStream(data);
		// for each Amstrad filename
		cats.clear();
		for (int c=0;c<data.length/0x20;c++) {
			DskSectorCatalog cat = new DskSectorCatalog(master);
			if (cat.scan(bis)) {
				cats.add(cat);
			}
		}
		bis.close();
	}

}
