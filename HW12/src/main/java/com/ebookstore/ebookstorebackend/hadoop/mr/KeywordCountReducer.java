package com.ebookstore.ebookstorebackend.hadoop.mr;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Reducer：对同一关键词的计数做求和。
 */
public class KeywordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable outValue = new IntWritable();

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable v : values) {
            if (v != null) {
                sum += v.get();
            }
        }
        outValue.set(sum);
        context.write(key, outValue);
    }
}
