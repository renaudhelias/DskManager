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
			int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
			for (int j=0;j<dskTrack.nbSectors;j++) {
				if (sectorId[j] <= 0xC4) {
					DskSectorCatalog sector = new DskSectorCatalog(sectorId[j],dskFile);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
				} else {
					DskSector sector = new DskSector(sectorId[j],dskFile);
					sector.scan(fos);
					dskTrack.sectors.add(sector);
				}
					
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
		fos.getChannel().position(0x100); //header
		//Track-info
		fos.getChannel().position(0x200); // first Track-info
		sectorCatalogC1.scan(fos,fileName);
		
		
		DskFile dskFile=new DskFile(currentDir, fileName);

		// garbage
		for (int k=0;k<0x200-0x160;k++) {
			fos.write(0);					
		}
		
		for (int j=0;j<track0.nbSectors;j++) {
			int [] sectorSizes = new int[] {0x80,0x100,0x200,0x400,0x800,0x1000,0x1800};
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
