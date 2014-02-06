package com.skunkworks.devtest.functionaltests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.BeforeClass;
import org.junit.Test;

import com.skunkworks.devtest.mr.WordFrequency;

public class WordFrequencyTest {

	private static final String inputFsPath = "/wordfreqinput";
	private static final String outputFsPath = "/wordfreqoutput";
	private static FileSystem fs;
	private static Configuration conf;

	@BeforeClass
	public static void runHadoopJobs() throws IOException, URISyntaxException {
		conf = new Configuration();
		fs = FileSystem.get(conf);
		URI uri = WordFrequencyTest.class.getClass()
				.getResource("/data/wordfrequency.txt").toURI();
		createInputInFs(uri, inputFsPath);

		String[] args = new String[3];
		args[0] = "-D mapred.reduce.tasks=1";
		args[1] = inputFsPath;
		args[2] = outputFsPath;
		WordFrequency.main(args);
	}

	@Test
	public void testWordFrequency() throws IOException {
		Map<String, Integer> map = getHadoopOutputAsMap(new Path(outputFsPath));
		assertEquals(map.get("which"), new Integer(8));
	}

	private static void createInputInFs(URI inputSFile, String inputSFsPath)
			throws IOException {
		Path inputFile = new Path(inputSFile);
		Path inputFsPath = new Path(inputSFsPath);
		fs.delete(inputFsPath, true);
		fs.mkdirs(inputFsPath);
		fs.copyFromLocalFile(inputFile, inputFsPath);
	}

	private static Map<String, Integer> getHadoopOutputAsMap(Path outputPath)
			throws IOException {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		Text key = new Text();
		IntWritable value = new IntWritable();
		for (FileStatus status : fs.listStatus(outputPath)) {
			Path path = status.getPath();
			if (!path.getName().contains("SUCCESS")) {
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, path,
						conf);
				while (reader.next(key, value)) {
					ret.put(key.toString(), value.get());
				}
				reader.close();
			}
		}
		return ret;
	}

}
