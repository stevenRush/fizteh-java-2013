package ru.fizteh.fivt.students.visamsonov.tests;

import org.junit.*;
import java.io.File;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.visamsonov.storage.TableDirectory;

public class TableProviderTest {

	private static TableProvider testedProvider;

	@Before
	public void before () throws Exception {
		new File("/tmp/dbtest/").mkdirs();
		testedProvider = new TableDirectory("/tmp/dbtest");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNull () throws Exception {
		testedProvider.createTable(null);
		testedProvider.getTable(null);
	}

	@Test
	public void testCreate () throws Exception {
		testedProvider.createTable("abcd");
		Assert.assertNull(testedProvider.createTable("abcd"));
	}

	@Test
	public void testGet () throws Exception {
		Assert.assertNull(testedProvider.getTable("abcdefgh"));
	}
}