package org.garret.perst.impl;

import org.garret.perst.*;


public class ReplicationMasterStorageImpl extends StorageImpl implements ReplicationMasterStorage
{ 
    public ReplicationMasterStorageImpl(int port, String[] hosts, int asyncBufSize) { 
        this.port = port;
        this.hosts = hosts;
        this.asyncBufSize = asyncBufSize;
    }
    
    public void open(IFile file, int pagePoolSize) {
        super.open(asyncBufSize != 0 
                   ? (ReplicationMasterFile)new AsyncReplicationMasterFile(this, file, asyncBufSize)
                   : new ReplicationMasterFile(this, file),
                   pagePoolSize);
    }

    public int getNumberOfAvailableHosts() { 
        return ((ReplicationMasterFile)pool.file).getNumberOfAvailableHosts();
    }

    int      port;
    String[] hosts;
    int      asyncBufSize;
}
