package dskmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
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
	 * @param type 
	 * @return
	 * @throws IOException
	 */
	public DskFile newDsk(File currentDir, String dskName, DskType type) throws IOException{
		int [] sectorId_SS40={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
		int [] sectorId_DOSD2={0x21,0x26,0x22,0x27,0x23,0x28,0x24,0x29,0x25};
		int [] sectorId=null;
		DskFile dskFile=new DskFile(currentDir, dskName);
		if (type == DskType.DOSD2) {
			sectorId=sectorId_DOSD2;
			dskFile.nbSides=2;
			dskFile.nbTracks=80;
		} else if (type == DskType.SS40) {
			sectorId=sectorId_SS40;
		}
		dskFile.master=new DskMaster();
		dskFile.master.type=type;
		FileOutputStream fos= new FileOutputStream(dskFile.file);
		dskFile.scan(fos);
		dskFile.master.allSectors.clear();
		for (int i=0; i<dskFile.nbTracks; i++) {
			for (int s=0; s<dskFile.nbSides; s++) {
				DskTrack dskTrack = new DskTrack(dskFile.master);
				if (type==DskType.DOSD2) {
					dskTrack.gap=0x52; // for tests
				}
				dskTrack.noTrack=i;
				dskFile.tracks.add(dskTrack);
				dskTrack.scan(fos);
				
				for (int j=0;j<dskTrack.nbSectors;j++) {
					// bon on est sur du 0xX1 0xX2 0xX3 0xX4
					DskSector sector = new DskSector(dskFile.master);
					sector.trackC=i;
					sector.sideH=s;
					sector.sectorIdR=sectorId[j];
					sector.scan(fos);
					if (dskFile.master.catalogToCreate(sector.trackC, sector.sideH, sector.sectorIdR)) {
						sector = new DskSectorCatalogs(sector);
					}
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
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
		}
		fos.close();
		// cats : on attache les secteurs pointé par la liste de sector cat
		for(DskSectorCatalogs catalog :dskFile.master.buildCatalogs(dskFile.tracks)){
			catalog.scanCatalogFromData();
		}
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
			for (int s=0; s<dskFile.nbSides; s++) {
				DskTrack dskTrack= new DskTrack(dskFile.master);
				dskFile.tracks.add(dskTrack);
				System.out.println("avant scan sdkTrack : "+fis.getChannel().position());
				dskTrack.scan(fis);
				for (int j=0;j<dskTrack.nbSectors;j++) {
					DskSector sector = new DskSector(dskFile.master);
					sector.scan(fis);
					if (i==0 && s==0 && j==0) {
						if ((sector.sectorIdR & 0xF0)==0xC0) {
							dskFile.master.type=DskType.SS40;
						} else if ((sector.sectorIdR & 0xF0)==0x20) {
							dskFile.master.type=DskType.DOSD2;
						}
					}
					if (dskFile.master.catalogToCreate(sector.trackC, sector.sideH, sector.sectorIdR)) {
						sector = new DskSectorCatalogs(sector);
					}
					dskTrack.sectors.add(sector);
					dskFile.master.allSectors.add(sector);
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
		}
		fis.close();
		// cats : on attache les secteurs pointé par la liste de sector cat
		for(DskSectorCatalogs catalog :dskFile.master.buildCatalogs(dskFile.tracks)){
			catalog.scanCatalogFromData();
			System.out.println("sectorId:"+catalog.sectorIdR+" side:"+catalog.sideH);
		}
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
		System.out.println("Récupération de C1-C2");
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		DskType type = dskFile.master.type;

		for (DskSectorCatalogs catalog : catalogsC1C4) {
			catalog.scanCatalog();
		}
		
		// file ici est la fichier dans le cat. Faut ouvrir le fichier lui même.
		File file = new File(currentDir,fileName);
		// deux sectors par catId, sectorSize=512Ko *2=1024Ko=0x400
		int nbEntry = (int)(file.length()/(0x400));
		int lastEntry = (int)(file.length()%(0x400));
		if (lastEntry>0) {
			nbEntry++;
		}
		// le transformer en cats
		List<DskSectorCatalog> catalogs = new ArrayList<DskSectorCatalog>();
		int countSectorIncrement=0;
		while (nbEntry>0) {
			DskSectorCatalog cat = new DskSectorCatalog(dskFile.master);
				// un cat a 10 entrées
				int entriesSectorCount=0x10;
				if (type==DskType.DOSD2) {
					entriesSectorCount=0x08;
				}
				for (int j=0;j<Math.min(nbEntry,entriesSectorCount);j++) {
					NewFreeCatResult cats = dskFile.master.nextFreeCat();
					// petit malin
					cat.catsId.add(cats.catId);
					cat.catsSector.addAll(cats.catSectors);
				}
				cat.sectorIncrement=countSectorIncrement;
				countSectorIncrement++;
				cat.filename=fileName;
				catalogs.add(cat);
			
			// travail ici avec entriesSector de taille 0x10
				
			
			nbEntry=nbEntry-entriesSectorCount;
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
				cat.sectorLength=Math.min(0x80, cat.catsId.size()*0x80/entriesSectorCount);
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
				d.data=new byte[Math.min(dskFile.master.sectorSizes[d.sectorSizeN],fis.available())];
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
		
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);
		
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

	public void eraseFile(DskFile dskFile, String fileName) throws IOException {
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);

		for (DskSectorCatalogs cat : catalogsC1C4) {
			for (DskSectorCatalog entryFile : cat.cats) {
				if (dskFile.master.cpcname2realname(entryFile.filename).equals(fileName)){
					entryFile.jocker=0xE5;
				}
			}
			
			RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
			cat.scanCatalog();
			cat.scanData(fos);
			fos.close();
		}
		for (DskSectorCatalogs catalogC1C4 : catalogsC1C4) {
			catalogC1C4.scanCatalogFromData();
		}
	}

	public LinkedHashMap<String,ByteArrayOutputStream> listFiles(DskFile dskFile) throws IOException {
		LinkedHashMap<String,ByteArrayOutputStream> listFiles = new LinkedHashMap<String,ByteArrayOutputStream>();
		List<DskSectorCatalogs> catalogsC1C4=dskFile.master.buildCatalogs(dskFile.tracks);

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
