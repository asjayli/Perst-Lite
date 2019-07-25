import org.garret.perst.*;

import java.util.*;

class Record extends Persistent { 
    String strKey;
    long   intKey;
};

class Indices extends Persistent {
    Index strIndex;
    Index intIndex;
}

public class TestIndex { 
    final static int nRecords = 100000;
    static int pagePoolSize = 32*1024*1024;

    static public void main(String[] args) {    
        Storage db = StorageFactory.getInstance().createStorage();
        boolean serializableTransaction = false;
        for (int i = 0; i < args.length; i++) { 
            if ("inmemory".equals(args[i])) { 
                pagePoolSize = Storage.INFINITE_PAGE_POOL;
            } else if ("altbtree".equals(args[i])) { 
                db.setProperty("perst.alternative.btree", Boolean.TRUE);
                //db.setProperty("perst.object.cache.kind", "weak");
                db.setProperty("perst.object.cache.init.size", new Integer(1013));
            } else if ("serializable".equals(args[i])) { 
                db.setProperty("perst.alternative.btree", Boolean.TRUE);
                serializableTransaction = true;
            } else if ("gc".equals(args[i])) { 
                db.setProperty("perst.gc.threshold", new Integer(1024*1024));
                db.setProperty("perst.background.gc", Boolean.TRUE);
            } else { 
                System.err.println("Unrecognized option: " + args[i]);
            }
        }
        //db.open("@testidx.mdf", pagePoolSize); // multifile
        db.open("testidx.dbs", pagePoolSize);

        if (serializableTransaction) { 
            db.beginThreadTransaction(Storage.SERIALIZABLE_TRANSACTION);
        }
            
        Indices root = (Indices)db.getRoot();
        if (root == null) { 
            root = new Indices();
            root.strIndex = db.createIndex(String.class, true);
            root.intIndex = db.createIndex(long.class, true);
            db.setRoot(root);
        }
        Index intIndex = root.intIndex;
        Index strIndex = root.strIndex;
        long start = System.currentTimeMillis();
        long key = 1999;
        int i;        
        for (i = 0; i < nRecords; i++) { 
            Record rec = new Record();
            key = (3141592621L*key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            intIndex.put(new Key(rec.intKey), rec);                
            strIndex.put(new Key(rec.strKey), rec);                
            /*
            if (i % 100000 == 0) { 
                System.out.print("Insert " + i + " records\r");
                db.commit();
            }
            */
        }
        
        if (serializableTransaction) { 
            db.endThreadTransaction();
        } else { 
            db.commit();
        }
        //db.gc();
        System.out.println("Elapsed time for inserting " + nRecords + " records: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");

        start = System.currentTimeMillis();
        key = 1999;
        for (i = 0; i < nRecords; i++) { 
            key = (3141592621L*key + 2718281829L) % 1000000007L;
            Record rec1 = (Record)intIndex.get(new Key(key));
            Record rec2 = (Record)strIndex.get(new Key(Long.toString(key)));
            Assert.that(rec1 != null && rec1 == rec2);
        }
        System.out.println("Elapsed time for performing " + nRecords*2 + " index searches: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");
 
        start = System.currentTimeMillis();
        Iterator iterator = intIndex.iterator();
        key = Long.MIN_VALUE;
        for (i = 0; iterator.hasNext(); i++) { 
            Record rec = (Record)iterator.next();
            Assert.that(rec.intKey >= key);
            key = rec.intKey;
        }
        Assert.that(i == nRecords);
        iterator = strIndex.iterator();
        String strKey = "";
        for (i = 0; iterator.hasNext(); i++) { 
            Record rec = (Record)iterator.next();
            Assert.that(rec.strKey.compareTo(strKey) >= 0);
            strKey = rec.strKey;
        }
        Assert.that(i == nRecords);
        System.out.println("Elapsed time for iterating through " + (nRecords*2) + " records: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");

        HashMap map = db.getMemoryDump();
        iterator = map.values().iterator();
        System.out.println("Memory usage");
        start = System.currentTimeMillis();
        while (iterator.hasNext()) { 
            MemoryUsage usage = (MemoryUsage)iterator.next();
            System.out.println(" " + usage.cls.getName() + ": instances=" + usage.nInstances + ", total size=" + usage.totalSize + ", allocated size=" + usage.allocatedSize);
        }
        System.out.println("Elapsed time for memory dump: " + (System.currentTimeMillis() - start) + " milliseconds");
        
        start = System.currentTimeMillis();
        key = 1999;
        if (serializableTransaction) { 
            db.beginThreadTransaction(Storage.SERIALIZABLE_TRANSACTION);
        }
        for (i = 0; i < nRecords; i++) { 
            key = (3141592621L*key + 2718281829L) % 1000000007L;
            Record rec = (Record)intIndex.get(new Key(key));
            Record removed = (Record)intIndex.remove(new Key(key));
            Assert.that(removed == rec);
            //strIndex.remove(new Key(Long.toString(key)), rec);
            strIndex.remove(new Key(Long.toString(key)));
            rec.deallocate();
        }
        if (serializableTransaction) { 
            db.endThreadTransaction();
        }
        Assert.that(!intIndex.iterator().hasNext());
        Assert.that(!strIndex.iterator().hasNext());
        Assert.that(!intIndex.iterator(null, null, Index.DESCENT_ORDER).hasNext());
        Assert.that(!strIndex.iterator(null, null, Index.DESCENT_ORDER).hasNext());
        Assert.that(!intIndex.iterator(null, null, Index.ASCENT_ORDER).hasNext());
        Assert.that(!strIndex.iterator(null, null, Index.ASCENT_ORDER).hasNext());
        System.out.println("Elapsed time for deleting " + nRecords + " records: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");
        db.close();
    }
}
