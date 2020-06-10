package dskmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

public class DskManagerTest {

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
	public void testDM() throws IOException {
		System.out.println(new File(currentDir,"jdvpa10_test2.dsk").delete());
		DskManager dm = DskManager.getInstance();
		DskFile toto = dm.newDsk(currentDir, "jdvpa10_test2.dsk");
		dm.addFile(toto,currentDir,"main.bin",false);
		compare(currentDir, "jdvpa10_test2.dsk", "jdvpa10_test0.dsk");
		
		DskFile tutu=dm.loadDsk(currentDir, "jdvpa10_test2.dsk");
		compare(toto,tutu);
	}

	private void compare(DskFile toto, DskFile tutu) {
		assertEquals(toto.master.catSectors.size(),tutu.master.catSectors.size());
		
		assertEquals(toto.tracks.size(),tutu.tracks.size());
		assertEquals(toto.tracks.get(0).sectors.size(),tutu.tracks.get(0).sectors.size());
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
