package dominus.intg.datastore.rocksdb;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class TestRocksDB extends DominusJUnit4TestBase {
    RocksDB db = null;
    Options options;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        options = new Options().setCreateIfMissing(true);
        db = null;
        try {
            // a factory method that returns a RocksDB instance
            db = RocksDB.open(options, "/tmp/rocksdb");
            // do something
        } catch (RocksDBException e) {
            // do some error handling
        }
        out.println(db.toString());
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        if (db != null) db.close();
        options.close();
    }

    @Test
    public void testHelloWorld() throws RocksDBException {
        db.put("Hello".getBytes(), "World".getBytes());

    }


}
