package dskmanager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DskSectorCatalogs extends DskSector {
	
	List<DskSectorCatalog> cats = new ArrayList<DskSectorCatalog>();
	public DskSectorCatalogs(DskMaster master,int track, int sectorId) {
		super(master,track,sectorId);
	}

	public List<Integer> scanCatalog(RandomAccessFile fos, String fileName, List<Integer> catalog) throws IOException {
		List<Integer>catalogDepil =new ArrayList<Integer>(catalog);
		Iterator<Integer> it = catalogDepil.iterator();
		while (cats.size()<=0x10 && it.hasNext()) {
			// ajouter des entrée
			DskSectorCatalog catEntry = new DskSectorCatalog(master);
			catEntry.filename=fileName;
			catEntry.catSectors.clear();
			while (it.hasNext()) {
				Integer catDepil = it.next();
				catEntry.catSectors.put(catDepil, master.allCats.get(catDepil));
			}
			// on écrit dans DATA C1
			catEntry.scan(fos, fileName);
			cats.add(catEntry);
		}
		scanData(fos);
		if (cats.size()== 0x10 && it.hasNext()) {
			//full, bye bye C1, goto C2.
			return null;
		} else {
			if (it.hasNext()) {
				List<Integer> babies = new ArrayList<Integer>();
				while (it.hasNext()) {
					babies.add(it.next());
				}
				return babies;
			} else {
				return new ArrayList<Integer>();
			}
		}
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
	public void scanCatalog() throws IOException {
		// fill cats from data
		ByteArrayInputStream bis=new ByteArrayInputStream(data);
		DskSectorCatalog cat = new DskSectorCatalog(master);
		// for each Amstrad filename
		for (int c=0;c<data.length/0x20;c++) {
			cat.scan(bis);
			cats.add(cat);
		}
		
	}

}
