package ru.fizteh.fivt.students.msandrikova.storeable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ru.fizteh.fivt.students.msandrikova.shell.Utils;

public class StoreableTableProviderFactory implements ChangesCountingTableProviderFactory, AutoCloseable {
	private boolean isClosed = false;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	private Set<ChangesCountingTableProvider> providers = new HashSet<ChangesCountingTableProvider>();

    @Override
    public ChangesCountingTableProvider create(String dir) 
    		throws IllegalArgumentException, IOException, IllegalStateException {
    	this.checkIsClosed();
        if (Utils.isEmpty(dir)) {
            throw new IllegalArgumentException("Directory can not be null.");
        }
        ChangesCountingTableProvider newTableProvider = null;
        try {
            newTableProvider = new StoreableTableProvider(new File(dir));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
        return newTableProvider;
    }

    private void checkIsClosed() throws IllegalStateException {
    	this.lock.readLock().lock();
    	if (this.isClosed) {
    		this.lock.readLock().unlock();
    		throw new IllegalStateException("Table privider factory was closed.");    		
    	}
    	this.lock.readLock().unlock();
    }
    
    public void close() throws IllegalStateException {
    	this.checkIsClosed();
    	
    	this.lock.writeLock().lock();
    	for (ChangesCountingTableProvider tableProvider : this.providers) {
    		tableProvider.close();
    	}
    	
    	this.isClosed = true;
    	this.lock.writeLock().unlock();
    }
}
