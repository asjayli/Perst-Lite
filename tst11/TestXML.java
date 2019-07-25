import org.garret.perst.*;
import org.garret.perst.impl.XMLExporter;
import org.garret.perst.impl.XMLImporter;

import java.io.*;
import java.util.Date;

public class TestXML 
{ 
    public static class Record extends Persistent 
    { 
        String strKey;
        long   intKey;
        double realKey;
        Date   timestamp;

        public Record() {}
        
        
        public void writeObject(IOutputStream out) {
            out.writeString(strKey);
            out.writeLong(intKey);
            out.writeDouble(realKey);
            out.writeDate(timestamp);
        }
        
        public void readObject(IInputStream in) {
            strKey = in.readString();
            intKey = in.readLong();
            realKey = in.readDouble();
            timestamp = in.readDate();
        }
    };
    
    public static class Indices extends Persistent {
        Index strIndex;
        Index intIndex;
        Index compoundIndex;

        public Indices() {}

        public void writeObject(IOutputStream out) {
            out.writeObject(strIndex);
            out.writeObject(intIndex);
            out.writeObject(compoundIndex);
        }
        
        public void readObject(IInputStream in) {
            strIndex = (Index)in.readObject();
            intIndex = (Index)in.readObject();
            compoundIndex = (Index)in.readObject();
        }
    }

    final static int nRecords = 100000;
    final static int pagePoolSize = 32*1024*1024;

    static public void main(String[] args) throws Exception {   
        Storage db = StorageFactory.getInstance().createStorage();
        db.open("test1.dbs", pagePoolSize);
        Indices root = (Indices)db.getRoot();
        if (root == null) { 
            root = new Indices();
            root.strIndex = db.createIndex(Types.String, true);
            root.intIndex = db.createIndex(Types.Long, true);
            root.compoundIndex = db.createIndex(new int[]{Types.String, Types.Double}, true);
            db.setRoot(root);
        }
        Index intIndex = root.intIndex;
        Index compoundIndex = root.compoundIndex;
        Index strIndex = root.strIndex;
        long start = System.currentTimeMillis();
        Date now = new Date(start);
        long key = 1999;
        int i;
        for (i = 0; i < nRecords; i++) { 
            Record rec = new Record();
            key = (3141592621L*key + 2718281829L) % 1000000007L;
            rec.intKey = key;
            rec.strKey = Long.toString(key);
            rec.realKey = (double)key;
            rec.timestamp = now;
            intIndex.put(new Key(rec.intKey), rec);                
            strIndex.put(new Key(rec.strKey), rec);                
            compoundIndex.put(new Key(rec.strKey, new Double(rec.realKey)), rec);                
        }
        db.commit();
        System.out.println("Elapsed time for inserting " + nRecords + " records: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");

        start = System.currentTimeMillis();
        Writer writer = new BufferedWriter(new FileWriter("test.xml"));
        XMLExporter exporter = new XMLExporter(db, writer);
        exporter.exportDatabase();
        writer.close();
        System.out.println("Elapsed time for XML export " + (System.currentTimeMillis() - start) + " milliseconds");
        db.close();
        db.open("test2.dbs", pagePoolSize);

        start = System.currentTimeMillis();
        Reader reader = new BufferedReader(new FileReader("test.xml"));
        XMLImporter importer = new XMLImporter(db, reader);
        importer.importDatabase();
        reader.close();
        System.out.println("Elapsed time for XML import " + (System.currentTimeMillis() - start) + " milliseconds");
        root = (Indices)db.getRoot();
        intIndex = root.intIndex;
        strIndex = root.strIndex;
        compoundIndex = root.compoundIndex;
        
        start = System.currentTimeMillis();
        key = 1999;
        for (i = 0; i < nRecords; i++) { 
            key = (3141592621L*key + 2718281829L) % 1000000007L;
            String strKey = Long.toString(key);
            Record rec1 = (Record)intIndex.get(new Key(key));
            Record rec2 = (Record)strIndex.get(new Key(strKey));
            Record rec3 = (Record)compoundIndex.get(new Key(strKey, new Double((double)key)));
            Assert.that(rec1 != null);
            Assert.that(rec1 == rec2);
            Assert.that(rec1 == rec3);
            Assert.that(now.getTime() >= rec1.timestamp.getTime() && now.getTime() - rec1.timestamp.getTime() < 1000);
            Assert.that(rec1.intKey == key);
            Assert.that(rec1.realKey == (double)key);
            Assert.that(strKey.equals(rec1.strKey));
        }
        System.out.println("Elapsed time for performing " + nRecords*2 + " index searches: " 
                           + (System.currentTimeMillis() - start) + " milliseconds");
        db.close();
    }
}




