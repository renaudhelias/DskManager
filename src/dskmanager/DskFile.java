package dskmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DskFile {
	
	private static final String HEADER_DISK="EXTENDED CPC DSK File\r\nDisk-Info\r\n";
	String creator="CPCDiskXP v2.5";
	byte nbTracks=40;
	byte nbSides=1;
	int sizeOfTrack=19;
	
	
	File file;
	List<DskTrack> tracks=new ArrayList<DskTrack>();
	public DskFile(File currentDir, String fileName) {
		file=new File(currentDir, fileName);
	}
	/**
	 * Scan from 0 to 0x0FF
	 * @param fis
	 */
	public void scan(FileInputStream fis) {
		
		
	}
	
	/**
	 * Scan from 0 to 0x0FF
	 * @param fis
	 * @throws IOException 
	 */
	public void scan(FileOutputStream fos) throws IOException {
		fos.write(HEADER_DISK.getBytes());
		fos.write(creator.getBytes());
		fos.write(nbTracks);
		fos.write(nbSides);
		fos.write(0);
		fos.write(sizeOfTrack);
		int i=0;
		for (i=0;i<nbTracks;i++) {
			fos.write(sizeOfTrack);
		}
		for (;i<204;i++) {
			fos.write(0);
		}
		
	}
	
}
