package dskmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class DskManagerTest {

	DskManager dm = DskManager.getInstance();
	
	File currentDir = new File(new File("src/"+DskManagerTest.class.getName().replace('.', '/')+".class").getAbsolutePath()).getParentFile();
	
	@Test
	public void test() throws InterruptedException {
		try {
			
			System.out.println(currentDir);
			System.out.println(new File(currentDir,"jdvpa10_test1.dsk").delete());
			Runtime.getRuntime().exec("CPCDiskXP -File main.bin -AddToNewDsk jdvpa10_test1.dsk",null,currentDir).waitFor();

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
		dm.addFile(toto,currentDir,"main.bin",false);
		compare(currentDir, "jdvpa10_test2.dsk", "jdvpa10_test1.dsk");
	}
	
	@Test
	public void testDMLoadDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		assertEquals(dskFile.tracks.size(),40);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		assertEquals(track0.sectors.size(),9);
	}
	@Test
	public void testDMLoadDskCatalog() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "jdvpa10_test1.dsk");
		assertEquals(dskFile.tracks.size(),40);
		DskTrack track0=dskFile.tracks.get(0);
		assertEquals(track0.sectors.size() ,9);
		assertEquals(track0.nbSectors,9);
		DskSectorCatalogs track0secC1=(DskSectorCatalogs)dskFile.master.find(track0,0xC1);
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
		File r1=dm.readFile(dskFile,currentDir,"MATRIX8F.SKS");
		File r2=dm.readFile(dskFile,currentDir,"CHIEFTAI.SKS");
		File r3=dm.readFile(dskFile,currentDir,"PAN.SKS");
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
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		dm.addFile(dskFile, currentDir, "main.bin", false);
		File r1=dm.readFile(dskFile,currentDir,"main.bin");
		assertNotNull(r1);
		assertTrue(r1.exists());
		assertTrue(r1.length()==0);
		dm.eraseFile(dskFile, currentDir, "main.bin");
		r1=dm.readFile(dskFile,currentDir,"main.bin");
		assertNotNull(r1);
		assertFalse(r1.exists());
	}

	@Test
	public void testDMListDsk() throws IOException {
		DskFile dskFile=dm.loadDsk(currentDir, "TRON-PIXEL.dsk");
		List<String> list=dm.listFiles(dskFile);
		assertNotNull(list);
		assertTrue(list.contains("TRON.BAS"));
	}
}
