package com.ebookstore.ebookstorebackend.hadoop.mr;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Mapper：统计关键词出现次数。
 *
 * 规则：
 * - 对于每个关键词，直接在文本中查找子串匹配（支持中英文混合）
 * - 每行文本中每个关键词出现一次就计数一次
 */
public class KeywordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);

    // 关键词列表：与 HadoopJobService 保持一致
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            // 计算机技术类
            "Java",
            "JavaScript",
            "Python",
            "C++",
            "编程",
            "算法",
            "数据结构",
            "设计模式",
            "代码",
            "软件",
            "系统",
            "程序员",
            "开发",
            // 文学类
            "小说",
            "故事",
            "文学",
            "人性",
            "生命",
            "爱情",
            "孤独",
            "命运"
    ));

    private final Text outKey = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        if (value == null) {
            return;
        }
        String line = value.toString();
        if (line == null || line.isBlank()) {
            return;
        }
        // 对每个关键词进行子串查找
        for (String keyword : KEYWORDS) {
            if (line.contains(keyword)) {
                // 统计该关键词在当前行中出现的次数
                int count = countOccurrences(line, keyword);
                for (int i = 0; i < count; i++) {
                    outKey.set(keyword);
                    context.write(outKey, ONE);
                }
            }
        }
    }

    /**
     * 统计子串在文本中出现的次数（支持重叠）
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += 1; // 允许重叠匹配
        }
        return count;
    }
}
