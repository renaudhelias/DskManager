package dskmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
				if (dskFile.master.sectorId[j] <= 0xC4 && i==0) {
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
		sectorCatalogC1.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalogFromData();
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
				// avant 0xC4 et track0
				if (dskFile.master.sectorId[j] <= 0xC4 && i==0) {
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
		sectorCatalogC1.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) dskFile.master.find(track0,0xC2);
		sectorCatalogC2.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) dskFile.master.find(track0,0xC3);
		sectorCatalogC3.scanCatalogFromData();
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) dskFile.master.find(track0,0xC4);
		sectorCatalogC4.scanCatalogFromData();
		
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
		System.out.println("Récupération de C1-C2");
		DskSectorCatalogs [] catalogsC1C4= {
			(DskSectorCatalogs) dskFile.master.find(track0,0xC1),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC2),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC3),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC4)
		};

		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			catalogC1C4.scanCatalog();
		}
		
		// file ici est la fichier dans le cat. Faut ouvrir le fichier lui même.
		File file = new File(currentDir,fileName);
		int nbEntry = (int)(file.length()/(dskFile.master.sectorSizes[2]*2));
		int lastEntry = (int)(file.length()%(dskFile.master.sectorSizes[2]*2));
		if (lastEntry>0) {
			nbEntry++;
		}
		// le transformer en cats
		List<DskSectorCatalog> catalogs = new ArrayList<DskSectorCatalog>();
		int countSectorIncrement=0;
		while (nbEntry>0) {
			DskSectorCatalog cat = new DskSectorCatalog(dskFile.master);
				// un cat a 10 entrées
				for (int j=0;j<Math.min(nbEntry,0x10);j++) {
					NewFreeCatResult cats = dskFile.master.nextFreeCat();
					// petit malin
					cat.catsId.add(cats.catId);
					cat.catsSector.addAll(cats.catSectors);
				}
				cat.sectorIncrement=countSectorIncrement;
				countSectorIncrement++;
				cat.filename=fileName;
				catalogs.add(cat);
			
			nbEntry=nbEntry-0x10;
			if (nbEntry>0) {
				// full cat
				cat.sectorLength=0x80;
			} else if (nbEntry%0x10==0) {
				// last full cat
				cat.sectorLength=0x80;
			} else {
				//cat.catsId.size()=9
				//9       =>0x48 72
				//16 0x10 =>0x80 128
				cat.sectorLength=Math.min(0x80, cat.catsId.size()*0x80/0x10);
			}
		}
		
		RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
		List<DskSectorCatalog> catalogsData= new ArrayList<DskSectorCatalog>(catalogs);
		// depile cat
		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			while (catalogC1C4.cats.size()<0x10 && !catalogs.isEmpty()) {
				catalogC1C4.cats.add(catalogs.get(0));
				catalogs.remove(0);
				// data from cats
				catalogC1C4.scanCatalog();
				catalogC1C4.scanData(fos);
			}
		}
		
		FileInputStream fis = new FileInputStream(file);
		for (DskSectorCatalog e:catalogsData) {
			for (DskSector d:e.catsSector) {
				d.data=new byte[Math.min(dskFile.master.sectorSizes[2],fis.available())];
				fis.read(d.data);
				d.scanData(fos);
			}
		}
		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			catalogC1C4.scanCatalogFromData();
		}
		fis.close();
		fos.close();
		
		
	}

	public File readFile(DskFile dskFile, File currentDir, String fileName) throws IOException {
		
		List<DskSector> sectors= new ArrayList<DskSector>();
		
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs [] catalogsC1C4= {
			(DskSectorCatalogs) dskFile.master.find(track0,0xC1),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC2),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC3),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC4)
		};
		for (DskSectorCatalogs cat : catalogsC1C4) {
			for (DskSectorCatalog entryFile : cat.cats) {
				if (dskFile.master.cpcname2realname(entryFile.filename).equals(fileName)){
					for (DskSector sector : entryFile.catsSector) {
						sectors.add(sector);
					}
				}
			}
		}
		
		File output=null;
		if (sectors.size()>0) {
			output= new File(currentDir,fileName);
			// create file
			FileOutputStream fos=new FileOutputStream(output);
			for(DskSector sector:sectors) {
				fos.write(sector.data);
			}
			fos.close();
		}
		return output;
	}

	public void eraseFile(DskFile dskFile, File currentDir, String fileName) throws IOException {
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs [] catalogsC1C4= {
			(DskSectorCatalogs) dskFile.master.find(track0,0xC1),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC2),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC3),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC4)
		};
		for (DskSectorCatalogs cat : catalogsC1C4) {
			for (DskSectorCatalog entryFile : cat.cats) {
				if (dskFile.master.cpcname2realname(entryFile.filename).equals(fileName)){
					entryFile.jocker=0xE5;
				}
			}
			cat.scanCatalog();
		}
		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			catalogC1C4.scanCatalogFromData();
		}
	}

	public LinkedHashMap<String,ByteArrayOutputStream> listFiles(DskFile dskFile) throws IOException {
		LinkedHashMap<String,ByteArrayOutputStream> listFiles = new LinkedHashMap<String,ByteArrayOutputStream>();
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalogs [] catalogsC1C4= {
			(DskSectorCatalogs) dskFile.master.find(track0,0xC1),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC2),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC3),
			(DskSectorCatalogs) dskFile.master.find(track0,0xC4)
		};
		for (DskSectorCatalogs cat : catalogsC1C4) {
			for (DskSectorCatalog entryFile : cat.cats) {
				
				for (DskSector sector:entryFile.catsSector) {
					String key = dskFile.master.cpcname2realname(entryFile.filename);
					if (listFiles.containsKey(key)) {
						listFiles.get(key).write(sector.data);
					} else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						baos.write(sector.data);
						listFiles.put(key,baos);
					}
					
				}
			}
		}
		return listFiles;
	}
	
}
