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
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 * Calculates the average temperature for each city, given a group of cities with
 * hourly temperature listing
 */
public class TemperatureAverages extends Configured implements Tool {

	/*
	 * Mapper Class
	 */
	private static class TemperatureMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String[] vals = value.toString().split(",");
			context.write(new Text(vals[0]),
					new IntWritable(Integer.parseInt(vals[2])));
		}
	}

	/*
	 * Reducer Class
	 */
	private static class TemperatureReducer extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			int length = 0;
			for (IntWritable temperature:values) {
				sum += temperature.get();
				length++;
			}
			context.write(key, new IntWritable(sum/length));
		}
	}

	/*
	 * Job setup and runner
	 * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
	 */
	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = this.getConf();
		
		Job job = new Job(conf, TemperatureAverages.class.getName());
		job.setJarByClass(TemperatureAverages.class);
		
		job.setMapperClass(TemperatureMapper.class);
		job.setReducerClass(TemperatureReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(TextInputFormat.class);

		Path out = new Path(args[1]);
		FileSystem fs = FileSystem.get(conf);
		fs.delete(out, true);
		
		FileOutputFormat.setOutputPath(job, out);
		job.setOutputFormatClass(org.apache.hadoop.mapreduce.lib.output.TextOutputFormat.class);
		
		return job.waitForCompletion(true) ? 0 : 1;
	}

	/*
	 * Entry point
	 */
	public static void main(String[] args) {
		try{
			ToolRunner.run(new TemperatureAverages(), args);
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
