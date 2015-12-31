package dominus.bigdata.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * MapReduce Tutorial V2.7.0
 * http://hadoop.apache.org/docs/r2.7.0/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
 * <p/>
 * [Hive Environment]
 * drop table shawguo_hive.saleslog;
 * create table shawguo_hive.SalesLog(prod_id string,cust_id bigint,time_id string,channel_id string, promo_id bigint, quantity_sold bigint, amount double, cost double)
 * ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
 * LOCATION "/user/shawguo/data/SalesLog"
 */
public class SalesCount {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private Text product_id = new Text();
        private IntWritable quantity_sold = new IntWritable();


        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            String[] fields = value.toString().split("\\|");


            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            /**
             * stdout
             [FileSplit] hdfs://scaj31cdh-ns/user/shawguo/data/SalesLog/sales1.dat:268435456+268435456
             [Path] hdfs://scaj31cdh-ns/user/shawguo/data/SalesLog/sales1.dat
             [Locations]
             */
            System.out.printf("[FileSplit] %s\n[Path] %s\n[Locations]\n", fileSplit, fileSplit.getPath(), fileSplit.getLocations().length);
//            System.out.println("Key:" + key + " Fields Length:" + fields.length + " " + ToStringBuilder.reflectionToString(fields));
            product_id.set(fields[0]);
            quantity_sold.set(Integer.valueOf(fields[5]));
            context.write(product_id, quantity_sold);
        }
    }

    /**
     * combiner: (input) <k1, v1> -> map -> <k2, v2> -> combine* -> <k2, v2> -> reduce -> <k3, v3> (output)
     * combiner input/output key/value types should be of the same type, while for the reducer this is not a requirement.
     */
    public static class LocalCombiner
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }


    public static class LongSumReducer
            extends Reducer<Text, IntWritable, Text, LongWritable> {
        private LongWritable result = new LongWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            long sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }


    /**
     * [Map Reduce]
     * Elapsed: 	1mins, 19sec
     * Diagnostics:
     * Average Map Time 	18sec
     * Average Shuffle Time 	17sec
     * Average Merge Time 	8sec
     * Average Reduce Time 	27sec
     * <p/>
     * >>> Combiner is enabled
     * Elapsed: 	28sec
     * Diagnostics:
     * Average Map Time 	18sec
     * Average Shuffle Time 	2sec
     * Average Merge Time 	0sec
     * Average Reduce Time 	0sec
     * <p/>
     * <p/>
     * <p/>
     * [Hive]
     * select prod_id, sum(quantity_sold) from saleslog group by prod_id order by prod_id asc
     * Hadoop job information for Stage-1: number of mappers: 9; number of reducers: 34
     * Hadoop job information for Stage-2: number of mappers: 4; number of reducers: 1
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");
        conf.addResource("yarn-clientconfig-cdh/mapred-site.xml");
        conf.addResource("yarn-clientconfig-cdh/yarn-site.xml"); //required for connecting to yarn cluster
        //Start by getting everything running (likely on a small input) in the local runner.
//        conf.set("mapred.job.tracker", "local");
        conf.set("keep.task.files.pattern", ".*_m_0000.*");

        Job job = Job.getInstance(conf, "SalesCount");
        /**
         *
         * cd /opt/Development/github_repo/my-charetype/archetype-helloworld/target/classes
         * rm /opt/Development/github_repo/archetype-helloworld/target/classes/wc.jar
         * jar cf wc.jar dominus/bigdata/mapreduce/SalesCount*.class log4j.properties
         */
        job.setJar("archetype-helloworld/target/classes/wc.jar");
        job.setInputFormatClass(TextInputFormat.class);

        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(LocalCombiner.class);
        job.setReducerClass(LongSumReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);


        job.setOutputFormatClass(TextOutputFormat.class);

        /**
         * It is legal to set the number of reduce-tasks to zero if no reduction is desired.
         *
         * In this case the outputs of the map-tasks go directly to the FileSystem, into the output path set by FileOutputFormat.setOutputPath(Job, Path).
         * The framework does not sort the map-outputs before writing them out to the FileSystem.
         */
        job.setNumReduceTasks(1);

        String outputSuffix = new SimpleDateFormat("yyyy_MM_dd_mm").format(new Date());
        FileInputFormat.addInputPath(job, new Path("/user/shawguo/data/SalesLog/sales1.dat"));
        FileOutputFormat.setOutputPath(job, new Path("/user/shawguo/data/SalesOutput_" +
                outputSuffix));
        System.out.println("OutputSuffix: " + outputSuffix);
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
