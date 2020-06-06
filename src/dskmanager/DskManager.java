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
		}
		fos.close();
	}

	public void addFile(File currentDir, String fileName, boolean generateAMSDOSHeader) throws IOException {
		
		//FileEntry
		File dskFileEntry=new File(currentDir, fileName);
		long size=dskFileEntry.length();
		int nbEntry = (int)(size/(16*1024))+1; // each 16KB
		int nbEntryLast = (int)(size%(16*1024)); // each 16KB
		
		// search entry free space
		FileInputStream fis = new FileInputStream(dskFile.file);
		fis.skip(0x100); //header
		//Track-info
		fis.skip(0x100); // first Track-info
		
		DskFile dskFile=new DskFile(currentDir, fileName);
		List<DskTrack> tracks=dskFile.tracks;
		// pour les 4 premiers secteur du premier track
		
				
		
		
		
	}
	
	

	private byte[] newEntrySectors() {
		
		return new byte [] {0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	}

	
	
}
