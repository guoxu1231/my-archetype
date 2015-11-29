package dominus.bigdata.mapred;

import java.io.IOException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/**
 * [Hive]
 * create table shawguo_hive.SalesLog(prod_id string,cust_id bigint,time_id string,channel_id string, promo_id bigint, quantity_sold bigint, amount double, cost double)
 * ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
 * LOCATION "/user/shawguo/data/SalesLog"
 */
public class SalesCount {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text product_id = new Text();
        private IntWritable quantity_sold = new IntWritable();


        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            String[] fields = value.toString().split("\\|");
//            System.out.println("Key:" + key + " Fields Length:" + fields.length + " " + ToStringBuilder.reflectionToString(fields));
            //TODO row validation
            product_id.set(fields[0]);
            quantity_sold.set(Integer.valueOf(fields[5]));
            context.write(product_id, quantity_sold);
        }
    }


    public static class IntSumReducer
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
     * Average Map Time 	1mins, 49sec
     * Average Shuffle Time 	19sec
     * Average Merge Time 	6sec
     * Average Reduce Time 	22sec
     * <p/>
     * [Hive]
     * select prod_id, sum(quantity_sold) from saleslog group by prod_id order by prod_id asc
     * Average Map Time	10sec
     * Average Shuffle Time	4sec
     * Average Merge Time	0sec
     * Average Reduce Time	1sec
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.addResource("hdfs-clientconfig-cdh/core-site.xml");
        conf.addResource("hdfs-clientconfig-cdh/hdfs-site.xml");
        conf.addResource("yarn-clientconfig-cdh/mapred-site.xml");
        conf.addResource("yarn-clientconfig-cdh/yarn-site.xml");
        Job job = Job.getInstance(conf, "SalesCount");

        //jar cf wc.jar dominus/bigdata/mapred/SalesCount*.class
        job.setJar("/opt/Development/github_repo/archetype-helloworld/target/classes/wc.jar");
        job.setMapperClass(TokenizerMapper.class);
//        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        FileInputFormat.addInputPath(job, new Path("/user/shawguo/data/SalesLog/sales1.dat"));
        FileOutputFormat.setOutputPath(job, new Path("/user/shawguo/data/sales1.output03.data"));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
