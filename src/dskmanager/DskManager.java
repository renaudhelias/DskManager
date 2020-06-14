package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DskManager {
	
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	/**
	 * .data puis scan(fos)
	 * @param currentDir
	 * @param dskName
	 * @return
	 * @throws IOException
	 */
	public DskFile newDsk(File currentDir, String dskName) throws IOException{
		DskFile dskFile=new DskFile(currentDir, dskName);
		dskFile.master=new DskMaster();
		FileOutputStream fos= new FileOutputStream(dskFile.file);
		dskFile.scan(fos);
		dskFile.master.allSectors.clear();
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack = new DskTrack(dskFile.master, i);
			dskFile.tracks.add(dskTrack);
			dskTrack.scan(fos);
			
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (dskFile.master.sectorId[j] <= 0xC4) {
					DskSectorCatalogs sector = new DskSectorCatalogs(dskFile.master, i, dskFile.master.sectorId[j]);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				} else {
					DskSector sector = new DskSector(dskFile.master, i, dskFile.master.sectorId[j]);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				}
					
			}
			//garbage "0" at end of Track-Info
			int garbage=0x1D-dskTrack.nbSectors;
			for (int j=0;j<garbage;j++) {
				for (int k=0;k<8;k++) {
					fos.write(0);
				}
			}
			
			// garbage "E5" as data of each sector
			for (int j=0;j<dskTrack.nbSectors;j++) {
				dskTrack.sectors.get(j).data=new byte[dskFile.master.sectorSizes[dskTrack.sectorSize]];
				for (int k=0;k<dskFile.master.sectorSizes[dskTrack.sectorSize];k++) {
					dskTrack.sectors.get(j).data[k]=((Integer)dskTrack.fillerByte).byteValue();
				}
				dskTrack.sectors.get(j).scanData(fos);
			}
			
			
			
		}
		fos.close();
		// cats : on attache les secteurs pointé par la liste de sector cat
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) dskFile.master.find(track0,0xC1);
		sectorCatalogC1.scanCatalog();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalog();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalog();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalog();
		return dskFile;
	}
	
	/**
	 * scan(fis) puis .data 
	 * @param currentDir
	 * @param dskName
	 * @return
	 * @throws IOException
	 */
	public DskFile loadDsk(File currentDir, String dskName) throws IOException {
		DskFile dskFile=new DskFile(currentDir, dskName);
		FileInputStream fis = new FileInputStream(dskFile.file);
		dskFile.scan(fis);
		dskFile.master.allSectors.clear();
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack= new DskTrack(dskFile.master,i);
			dskFile.tracks.add(dskTrack);
			System.out.println("avant scan sdkTrack : "+fis.getChannel().position());
			dskTrack.scan(fis);
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (dskFile.master.sectorId[j] <= 0xC4) {
					DskSector sector = new DskSectorCatalogs(dskFile.master,i, dskFile.master.sectorId[j]);
					System.out.println("avant scan sector : "+fis.getChannel().position());
					sector.scan(fis);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				} else {
					DskSector sector = new DskSector(dskFile.master,i, dskFile.master.sectorId[j]);
					System.out.println("avant scan sector : "+fis.getChannel().position());
					sector.scan(fis);
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
				}
					
			}
			System.out.println("garbage 0 debut : "+fis.getChannel().position());
			fis.skip(160);//0x100-0x60-dskTrack.nbSectors*8); // skip 0x00
			
			System.out.println("garbage 0 fin : "+fis.getChannel().position());
			for (DskSector sector : dskTrack.sectors) {
				System.out.println("avant scanData sector : "+fis.getChannel().position());
				sector.scanData(fis);
			}
			System.out.print("haouh");
		}
		fis.close();
		// cats : on attache les secteurs pointé par la liste de sector cat
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) dskFile.master.find(track0,0xC1);
		sectorCatalogC1.scanCatalog();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalog();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalog();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalog();
		
		return dskFile;
	}

	/**
	 * .data puis scan(fos) at position pour scanDATA+scanDATA
	 * @param dskFile
	 * @param currentDir
	 * @param fileName
	 * @param generateAMSDOSHeader
	 * @throws IOException
	 */
	public void addFile(DskFile dskFile, File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		DskTrack track0 = dskFile.tracks.get(0);
		System.out.println("Récupération de C1-C4");
		DskSectorCatalogs [] catalogsC1C4= {
			(DskSectorCatalogs) dskFile.master.find(track0,0xC1),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC2),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC3),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC4)};
		
		
//		fos.getChannel().position(0x100); //header
		//Track-info
//		; // first Track-info
		// FIXME : file est faux, faut ouvrir le fichier lui même.
		

		File file = new File(currentDir,fileName);
		FileInputStream fis = new FileInputStream(file);
		RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
		int nbEntry = (int)(file.length()/(dskFile.master.sectorSizes[2]));
		int lastEntry = (int)(file.length()%(dskFile.master.sectorSizes[2]));
		List<Integer> catalog = new ArrayList<Integer>();
		for (int i=0;i<=nbEntry;i++) {
			if (i<nbEntry || (i==nbEntry && lastEntry <i)) {
				int cat = dskFile.master.nextFreeCat();
				catalog.add(cat);
				DskSector d=dskFile.master.allCats.get(cat);
				d.data=new byte[Math.min(dskFile.master.sectorSizes[2],fis.available())];
				fis.read(d.data);
				d.scanData(fos);
			}
		}
		fis.close();
		
		
		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			// on se base sur le data ?
			List<Integer> suite = catalogC1C4.scanCatalog(fos, fileName, catalog);
			if (suite == null) {
				continue;
			} else if (suite.isEmpty()) {
				break;
			} else {
				catalog=suite;
			}
		}
		
		
		
//		dskFile=new DskFile(currentDir, fileName);

		// garbage
//		for (int k=0;k<0x200-0x160;k++) {
//			fos.write(0);					
//		}
		
//		for (int j=0;j<track0.nbSectors;j++) {
//			for (int k=0;k<dskFile.master.sectorSizes[0x02];k++) {
//				fos.write(track0.fillerByte);
//			}
//		}
		
		fos.close();
		
	}
	
}
