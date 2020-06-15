package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DskSectorCatalogs extends DskSector {
	
	List<DskSectorCatalog> cats = new ArrayList<DskSectorCatalog>();
	public DskSectorCatalogs(DskMaster master,int track, int sectorId) {
		super(master,track,sectorId);
	}

	public void scanCatalog() throws IOException {
		// fill data from cats
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (DskSectorCatalog cat:cats) {
			cat.scan(baos);
		}

		data = baos.toByteArray();
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
		DskSectorCatalog cat = new DskSectorCatalog(master);
		// for each Amstrad filename
		cats.clear();
		for (int c=0;c<data.length/0x20*2;c++) {
			if (cat.scan(bis)) {
				cats.add(cat);
			}
		}
		bis.close();
	}

}
