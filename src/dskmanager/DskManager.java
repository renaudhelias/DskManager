package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DskManager {
	

	DskFile dskFile;
	
	int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
	int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
	// dictionary
	byte[]usedSectorEntry={};
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	public void newDsk(File currentDir, String dskName) throws IOException{
		dskFile=new DskFile(currentDir, dskName);
		FileOutputStream fos= new FileOutputStream(dskFile.file);
		dskFile.scan(fos);
		for (int i=0; i<dskFile.nbTracks; i++) {
			DskTrack dskTrack = new DskTrack(i);
			dskFile.tracks.add(dskTrack);
			dskTrack.scan(fos);
			
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (sectorId[j] <= 0xC4) {
					DskSectorCatalog sector = new DskSectorCatalog(i, sectorId[j],dskFile);
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
		fos.close();
	}

	public void addFile(File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		
		//FileEntry
		File dskFileEntry=new File(currentDir, fileName);
		long size=dskFileEntry.length();
		
		DskTrack track0 = dskFile.tracks.get(0);
		DskSectorCatalog sectorCatalogC1 = (DskSectorCatalog) track0.find(0xC1);
		DskSectorCatalog sectorCatalogC2 = (DskSectorCatalog) track0.find(0xC2);
		DskSectorCatalog sectorCatalogC3 = (DskSectorCatalog) track0.find(0xC3);
		DskSectorCatalog sectorCatalogC4 = (DskSectorCatalog) track0.find(0xC4);
		// search entry free space
		RandomAccessFile fos = new RandomAccessFile(dskFile.file, "rw");
//		fos.getChannel().position(0x100); //header
		//Track-info
//		; // first Track-info

		int nbEntry = (int)(dskFile.file.length()/(16*1024))+1; // each 16KB
//		

		FileInputStream fis=new FileInputStream(dskFile.file);
		List<DskSector> listSector=new ArrayList<DskSector>();
		for (int i=0;i<nbEntry;i++) {
			byte [] data=new byte[Math.min(512,fis.available())]; 
			fis.read(data);
			DskSector d=dskFile.nextFreeSector();
			d.data=data;
			listSector.add(d);
		}
		
		sectorCatalogC1.scan(fos.getChannel().position(0x200),fileName,listSector);
		fis.close();
		
		
		DskFile dskFile=new DskFile(currentDir, fileName);

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
	
	
	
	private int computeSectorSize(byte sectorSize) {
		//();
		if (sectorSize==2) return 0x200;  
		return 0;
	}

	private byte[] newEntrySectors() {
		
		return new byte [] {0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	}

	
	
}
