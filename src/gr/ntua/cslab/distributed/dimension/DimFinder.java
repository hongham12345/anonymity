package gr.ntua.cslab.distributed.dimension;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class DimFinder {
	private String qid;
	private JobConf job;
	public void runDimFinder(String inputDir, String outputDir) throws IOException{
		job = new JobConf(DimFinder.class);
		job.setJobName("dimension finder");
		job.set("qid", this.qid);
		
		job.setMapperClass(DimFinderMapper.class);
		job.setReducerClass(DimFinderReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);
		
		FileInputFormat.setInputPaths(job, new Path(inputDir));
		FileOutputFormat.setOutputPath(job, new Path(outputDir));
		
		JobClient.runJob(job);
	}
	
	public void setQid(String qid){
		this.qid=qid;
	}
	
	public int getDimension(){
		Path results = new Path(FileOutputFormat.getOutputPath(this.job).toString()+"/part-00000");
		try {
			FileSystem fs = FileSystem.get(this.job);
			FSDataInputStream in=fs.open(results);
			@SuppressWarnings("deprecation")
			String buffer=in.readLine();
			in.close();
			return new Integer(buffer.split("\t")[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public void clean(){
		try {
			FileSystem fs = FileSystem.get(this.job);
			fs.delete(FileOutputFormat.getOutputPath(this.job),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws IOException{
		if(args.length<2){
			System.err.println("I need input and output directories");
			return;
		}
		DimFinder a = new DimFinder();
		a.setQid("2 5 7");
		a.runDimFinder(args[0],args[1]);
	}
}