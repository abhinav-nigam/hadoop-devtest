package com.skunkworks.devtest.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 * Maps the words appearing in a file against the number of times they
 * are found. Implements Tool to simplify job configuration via cli params
 */
public class WordFrequency extends Configured implements Tool {

	private static final IntWritable one = new IntWritable(1);
	private static final Text word = new Text();
	private static final IntWritable count = new IntWritable(0);

	/*
	 * Mapper class
	 */
	private static class WordFrequencyMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			for (String token : value.toString().split(" ")) {
				word.set(token);
				context.write(word, one);
			}
		}
	}

	/*
	 * Reducer class
	 */
	private static class WordFrequencyReducer extends
			Reducer<Text, IntWritable, IntWritable, Text> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			count.set(0);
			int intCount = 0;
			for (IntWritable value : values) {
				intCount += value.get();
			}
			count.set(intCount);
			context.write(count, key);
		}
	}

	/*
	 * Entry point
	 */
	public static void main(String[] args) {
		try {
			System.exit(ToolRunner.run(new Configuration(),
					new WordFrequency(), args));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();

		Job job = new Job(conf, WordFrequency.class.getName());
		job.setJarByClass(WordFrequency.class);

		job.setMapperClass(WordFrequencyMapper.class);
		job.setReducerClass(WordFrequencyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		Path out = new Path(args[1]);
		FileSystem fs = FileSystem.get(conf);
		fs.delete(out,true);
		
		FileOutputFormat.setOutputPath(job, out);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		return job.waitForCompletion(true) ? 0 : 1;
	}
}
