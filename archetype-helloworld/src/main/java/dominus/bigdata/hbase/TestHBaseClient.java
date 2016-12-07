package dominus.bigdata.hbase;


import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Test;

import java.io.IOException;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public class TestHBaseClient extends DominusJUnit4TestBase {

    private static final String HBASE_TEST_TABLE = "HBASE_TEST_TABLE";
    private static final String CF_DEFAULT = "DEFAULT_COLUMN_FAMILY";

    Admin admin;
    Connection connection;

    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        //load hbase-default.xml and hbase-site.xml from java classpath.
        //habas-default.xml is packaged inside HBase.jar, hbase-site.xml will need to be added to the CLASSPATH.
        Configuration conf = new Configuration();
        conf.addResource(String.format("cdh-clientconfig/%s/hbase/hbase-site.xml", activeProfile()));
        //kerberos config
        if ("kerberos".equals(properties.getProperty("hadoop.security.authentication"))) {
            conf.setBoolean("hadoop.security.authorization", true);
            conf.setStrings("hadoop.security.authentication", "kerberos");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(properties.getProperty("hadoop.user"), properties.getProperty("hadoop.kerberos.keytab"));
        }
        Configuration hbaseConfig = HBaseConfiguration.create(conf);
        connection = ConnectionFactory.createConnection(hbaseConfig);
        admin = connection.getAdmin();

        //construct HTable
        HTableDescriptor table = new HTableDescriptor(TableName.valueOf(HBASE_TEST_TABLE));
        table.addFamily(new HColumnDescriptor(CF_DEFAULT).setCompressionType(Algorithm.NONE));

        logger.info("Creating HTable {}", HBASE_TEST_TABLE);
        createOrOverwrite(admin, table);
//        admin.gett

    }

    private void createOrOverwrite(Admin admin, HTableDescriptor table) throws IOException {
        if (admin.tableExists(table.getTableName())) {
            admin.disableTable(table.getTableName());
            admin.deleteTable(table.getTableName());
        }
        admin.createTable(table);
    }


    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        connection.close();
    }

    @Test
    public void testHTableCRUD() throws IOException {
        Table table = connection.getTable(TableName.valueOf(HBASE_TEST_TABLE));
        Put put1 = new Put(Bytes.toBytes("row1"));
        put1.addColumn(toBytes(CF_DEFAULT), toBytes("col1"), toBytes("val1"));
        put1.addColumn(toBytes(CF_DEFAULT),toBytes("col2"),toBytes("val2"));
        table.put(put1);
        table.close();
    }
}
