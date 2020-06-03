package dskmanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DskManager {
	
	public static String HEADER="EXTENDED CPC DSK File\r\nDisk-Info\r\n";
	public static String NAME_OF_CREATOR="CPCDiskXP v2.5";
	public static String HEADER_TRACK="Track-Info\r\n";
	File dskFile;
	byte nbTracks=40;
	byte nbSides=1;
	int sizeOfTrack=19;
	byte sectorSize=2;
	byte nbSectors=9;
	byte gap=0x2A;
	int fillerByte=0xE5;
	
	
	private static DskManager instance=null;
	public static DskManager getInstance(){
		if (instance==null) {instance=new DskManager();}
		return instance;
	}
	
	public void newDsk(File currentDir, String dskName) throws IOException{
		dskFile=new File(currentDir, dskName);
		OutputStream os= new FileOutputStream(dskFile); 
		os.write(HEADER.getBytes());
		os.write(NAME_OF_CREATOR.getBytes());
		os.write(nbTracks);
		os.write(nbSides);
		os.write(0);
		os.write(sizeOfTrack);
		int i=0;
		for (i=0;i<nbTracks;i++) {
			os.write(sizeOfTrack);
		}
		
		for (;i<204;i++) {
			os.write(0);
		}
		for (i=0;i<nbTracks;i++) {
			os.write(HEADER_TRACK.getBytes());
			os.write(0);os.write(0);os.write(0);os.write(0);
			os.write(i);
			os.write(0);// side
			os.write(0);os.write(0);
			os.write(sectorSize);
			os.write(nbSectors);
			os.write(gap);
			os.write(fillerByte);
			
			// SECTOR INFOS
			int [] sectorId={0xC1,0xC6,0xC2,0xC7,0xC3,0xC8,0xC4,0xC9,0xC5};
			for (int j=0;j<nbSectors;j++) {
				os.write(i);//track
				os.write(0);//side
				os.write(sectorId[j]);
				os.write(sectorSize);
				os.write(0);//FDC 1
				os.write(0);//FDC 2
				os.write(0);os.write(2);
			}
			// garbage
			for (int k=0;k<0x200-0x160;k++) {
				os.write(0);					
			}
			for (int j=0;j<nbSectors;j++) {
				for (int k=0;k<computeSectorSize(sectorSize);k++) {
					os.write(fillerByte);
				}
			}
		}
		os.close();
	}
	private int computeSectorSize(byte sectorSize) {
		//(x"80",x"100",x"200",x"400",x"800",x"1000",x"1800");
		if (sectorSize==2) return 0x200;  
		return 0;
	}

	public void addFile(File file, boolean generateAMSDOSHeader) {
		
	}
	
}
