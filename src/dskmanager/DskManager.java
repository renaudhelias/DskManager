package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DskManager {
	int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
	int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
	// dictionary
	byte[]usedSectorEntry={};
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	public DskFile newDsk(File currentDir, String dskName) throws IOException{
		DskFile dskFile=new DskFile(currentDir, dskName);
		FileOutputStream fos= new FileOutputStream(dskFile.file);
		dskFile.scan(fos);
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack = new DskTrack(i);
			dskFile.tracks.add(dskTrack);
			dskTrack.scan(fos);
			
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (sectorId[j] <= 0xC4) {
					DskSectorCatalogs sector = new DskSectorCatalogs(i, sectorId[j],dskFile);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
				} else {
					DskSector sector = new DskSector(i, sectorId[j],dskFile);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
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
				dskTrack.sectors.get(j).data=new byte[sectorSizes[dskTrack.sectorSize]];
				for (int k=0;k<sectorSizes[dskTrack.sectorSize];k++) {
					dskTrack.sectors.get(j).data[k]=((Integer)dskTrack.fillerByte).byteValue();
				}
				dskTrack.sectors.get(j).scanData(fos);
			}
		}
		dskFile.generateCatSectors();
		fos.close();
		return dskFile;
	}
	
	public DskFile loadDsk(File currentDir, String dskName) {
		DskFile dskFile=new DskFile(currentDir, dskName);
		return dskFile;
	}

	public void addFile(DskFile dskFile, File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		DskTrack track0 = dskFile.tracks.get(0);
		System.out.println("R�cup�ration de C1-C4");
		DskSectorCatalogs sectorCatalogC1 = (DskSectorCatalogs) track0.find(0xC1);
		System.out.println(sectorCatalogC1);
		DskSectorCatalogs sectorCatalogC2 = (DskSectorCatalogs) track0.find(0xC2);
		DskSectorCatalogs sectorCatalogC3 = (DskSectorCatalogs) track0.find(0xC3);
		DskSectorCatalogs sectorCatalogC4 = (DskSectorCatalogs) track0.find(0xC4);
		// search entry free space
		RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
//		fos.getChannel().position(0x100); //header
		//Track-info
//		; // first Track-info
		int nbEntry = (int)(dskFile.file.length()/(16*1024)); // each 16KB
		int lastEntry = (int)(dskFile.file.length()%(16*1024));

		FileInputStream fis=new FileInputStream(dskFile.file);
		List<DskSector> listSector=new ArrayList<DskSector>();
		for (int i=0;i<=nbEntry;i++) {
			if (i<nbEntry || (i==nbEntry && lastEntry <i)) {
				byte [] data=new byte[Math.min(512,fis.available())];
				fis.read(data);
				DskSector d=dskFile.nextFreeSector();
				d.data=data;
				listSector.add(d);
			}
		}
		
		sectorCatalogC1.scanCatalog(fos.getChannel().position(0x200),fileName,listSector);
		System.out.println("Apr�s : "+sectorCatalogC1);
		fis.close();
		
		
		dskFile=new DskFile(currentDir, fileName);

		// garbage
		for (int k=0;k<0x200-0x160;k++) {
			fos.write(0);					
		}
		
		for (int j=0;j<track0.nbSectors;j++) {
			for (int k=0;k<sectorSizes[0x02];k++) {
				fos.write(track0.fillerByte);
			}
		}
		
		fos.close();
		
	}
	
}
