package dskmanager;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class DskManagerTest {

	DskManager dm = DskManager.getInstance();
	
	File currentDir = new File(new File("src/"+DskManagerTest.class.getName().replace('.', '/')+".class").getAbsolutePath()).getParentFile();
	
	@Test
	public void test() throws InterruptedException {
		try {
			
			System.out.println(currentDir);
			System.out.println(new File(currentDir,"jdvpa10_test1.dsk").delete());
			Runtime.getRuntime().exec("CPCDiskXP -File main2.bin -AddToNewDsk jdvpa10_test1.dsk",null,currentDir).waitFor();

			FileInputStream fis = new FileInputStream(new File(currentDir,"jdvpa10_test1.dsk"));
			
			assertEquals(fis.read(),69);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("exe not found");
		}
	}
	
	@Test
	public void testDMCreateAddDsk() throws IOException {
		System.out.println(new File(currentDir,"jdvpa10_test2.dsk").delete());
		
		DskFile toto = dm.newDsk(currentDir, "jdvpa10_test2.dsk");
		dm.addFile(toto,currentDir,"main2.bin",false);
		compare(currentDir, "jdvpa10_test2.dsk", "jdvpa10_test1.dsk");
	}
	
	@Test
	public void testDMLoadDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		assertEquals(dskFile.tracks.size(),40);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.side,0);
		assertEquals(track0.nbSectors,9);
		assertEquals(track0.sectorSize,2);
		assertEquals(track0.sectors.size(),9);
		DskTrack track1=dskFile.tracks.get(1);
		assertEquals(track1.side,0);
	}

	@Test
	public void testDMLoadDskDOSD2() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "dosd2.dsk");
		assertEquals(dskFile.tracks.size(),80); // so SIZE * 2
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		assertEquals(track0.side,0);
		assertEquals(track0.sectorSize,2);
		assertEquals(track0.sectors.size(),9);
		DskTrack track1=dskFile.tracks.get(1);
		assertEquals(track1.side,1); // so SIZE * 2 * 2
	}
	
	@Test
	public void testDMLoadDskCatalog() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "jdvpa10_test1.dsk");
		assertEquals(dskFile.tracks.size(),40);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		DskSectorCatalogs track0secC1=(DskSectorCatalogs)dskFile.master.find0F(track0,0xC1);
		DskSectorCatalog cat0 = track0secC1.cats.get(0) ;
		assertEquals(track0.sectors.size(),9);
		DskSector sector0 = cat0.catsSector.get(0);
		assertNotNull(sector0);
		assertEquals(cat0.catsSector.size(),9*2);
	}

	private void compare(File currentDir, String file1, String file2) throws IOException {
		FileInputStream fis1 = new FileInputStream(new File(currentDir,file1));
		FileInputStream fis2 = new FileInputStream(new File(currentDir,file2));
		int offset=0;
		while (fis1.available()>0 && fis2.available()>0) {
			offset++;
			assertEquals("at offset "+String.format("#%02X",offset),fis1.read(),fis2.read());
		}
		assertTrue(fis1.available()==0 && fis2.available()==0);
		fis1.close();
		fis2.close();
	}
	
	
	@Test
	public void testDMReadDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		new File(currentDir,"MATRIX8F.SKS").delete();
		File r1=dm.readFile(dskFile,currentDir,"MATRIX8F.SKS");
		new File(currentDir,"CHIEFTAI.SKS").delete();
		File r2=dm.readFile(dskFile,currentDir,"CHIEFTAI.SKS");
		new File(currentDir,"PAN.SKS").delete();
		File r3=dm.readFile(dskFile,currentDir,"PAN.SKS");
		new File(currentDir,"THRONES.SKS").delete();
		File r4=dm.readFile(dskFile,currentDir,"THRONES.SKS");
		assertNotNull(r1);
		assertTrue(r1.exists());
		assertNotNull(r2);
		assertTrue(r2.exists());
		assertNotNull(r3);
		assertTrue(r3.exists());
		assertNotNull(r4);
		assertTrue(r4.exists());
	}
	
	@Test
	public void testDMEraseDsk() throws IOException {
		DskFile dskFile=dm.newDsk(currentDir, "jdvpa10_test3.dsk");
		File original=new File(currentDir,"main.bin");
		File tmp=new File(currentDir,"main3.bin");
		tmp.delete();
		
		InputStream in = new BufferedInputStream(new FileInputStream(original));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
		  
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, lengthRead);
            out.flush();
        }
		out.close();
		in.close();
		
		dm.addFile(dskFile, currentDir, "main3.bin", false);
		File r1=dm.readFile(dskFile,currentDir,"MAIN3.BIN");
		assertNotNull(r1);
		assertTrue(r1.exists());
		tmp.delete();
		dm.eraseFile(dskFile, "MAIN3.BIN");
		r1=dm.readFile(dskFile,currentDir,"MAIN3.BIN");
		assertNull(r1);
		
		Set<String> list=dm.listFiles(dskFile).keySet();
		for(String notErased:list) {
			if (notErased.equals("MAIN3.BIN")) {
				fail("still here");
			}
		}
	}

	@Test
	public void testDMListDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		Set<String> list=dm.listFiles(dskFile).keySet();
		assertNotNull(list);
		assertTrue(list.contains("TRON.BAS"));
		assertTrue(list.contains("MATRIX8F.SKS"));
	}
}
