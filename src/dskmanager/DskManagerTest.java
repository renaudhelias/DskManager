package dskmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
//		compare(currentDir, "jdvpa10_test2.dsk", "jdvpa10_test0.dsk");
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
		DskSector sector0 = cat0.catSectors.get(0);
//		assertNull(sector0);
		assertEquals(cat0.catSectors.size(),9);
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

}
