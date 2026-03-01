package com.ebookstore.ebookstorebackend.hadoop.service;

import com.ebookstore.ebookstorebackend.hadoop.mr.KeywordCountMapper;
import com.ebookstore.ebookstorebackend.hadoop.mr.KeywordCountReducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class HadoopJobService {

    private static final String HDFS_DEFAULT_FS = "hdfs://localhost:9000";
    private static final org.apache.hadoop.fs.Path HDFS_INPUT_DIR = new org.apache.hadoop.fs.Path("/input/bookstore");
    private static final org.apache.hadoop.fs.Path HDFS_OUTPUT_DIR = new org.apache.hadoop.fs.Path("/output/keyword_stats");


    public Map<String, Integer> analyzeKeywords() throws Exception {
        // 设置 Hadoop Home（Windows 必需）
        System.setProperty("hadoop.home.dir", "D:\\Program Files\\Hadoop\\hadoop-3.3.6");
        
        Path localInputDir = prepareLocalData();

        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", HDFS_DEFAULT_FS);

        conf.set("dfs.client.use.datanode.hostname", "true");

        conf.set("dfs.datanode.use.datanode.hostname", "true");

        // 在类中添加静态代码块
        try {
                // 这里的路径替换为你实际存放 hadoop.dll 的绝对路径
                System.load("D:\\Program Files\\Hadoop\\hadoop-3.3.6\\bin\\hadoop.dll");
            }
             catch (UnsatisfiedLinkError e) {
                System.err.println("Native code library failed to load.\n" + e);
                System.exit(1);
              }

        try (FileSystem fs = FileSystem.get(URI.create(HDFS_DEFAULT_FS), conf)) {
            uploadToHdfs(fs, localInputDir);
            runJob(conf, fs);
            return readOutput(fs);
        }
    }

    /**
     * 生成本地输入数据（从 resources/hadoop-input/ 读取预先准备的书籍简介文件）。
     * 
     * 说明：按图书类型分类的简介文件存放在 src/main/resources/hadoop-input/ 下：
     *  - CS.txt: 计算机类书籍（8本）
     *  - Fiction.txt: 科幻小说（1本）
     *  - Mystery.txt: 推理小说（1本）
     *  - Literature.txt: 文学作品（6本）
     */
    public Path prepareLocalData() throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), "ebookstore-hadoop-input");
        Files.createDirectories(dir);

        // 从 classpath 复制预定义的书籍简介文件到本地临时目录
        String[] inputFiles = {"CS.txt", "Fiction.txt", "Mystery.txt", "Literature.txt"};
        
        for (String fileName : inputFiles) {
            ClassPathResource resource = new ClassPathResource("hadoop-input/" + fileName);
            if (!resource.exists()) {
                throw new IOException("找不到输入文件: hadoop-input/" + fileName);
            }
            
            try (InputStream in = resource.getInputStream()) {
                Path targetFile = dir.resolve(fileName);
                Files.copy(in, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }

        return dir;
    }

    private void uploadToHdfs(FileSystem fs, Path localInputDir) throws IOException {
        if (!fs.exists(HDFS_INPUT_DIR)) {
            fs.mkdirs(HDFS_INPUT_DIR);
        }

        // 上传目录下所有文件到 HDFS 输入目录
        try (var stream = Files.list(localInputDir)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                try {
                    org.apache.hadoop.fs.Path local = new org.apache.hadoop.fs.Path(p.toAbsolutePath().toString());
                    org.apache.hadoop.fs.Path dest = new org.apache.hadoop.fs.Path(HDFS_INPUT_DIR, p.getFileName().toString());
                    fs.copyFromLocalFile(false, true, local, dest);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void runJob(Configuration conf, FileSystem fs) throws Exception {
        // 清理旧输出
        if (fs.exists(HDFS_OUTPUT_DIR)) {
            fs.delete(HDFS_OUTPUT_DIR, true);
        }

        Job job = Job.getInstance(conf, "ebookstore-keyword-count");
        job.setJarByClass(HadoopJobService.class);

        job.setMapperClass(KeywordCountMapper.class);
        job.setReducerClass(KeywordCountReducer.class);
        job.setCombinerClass(KeywordCountReducer.class);
        job.setNumReduceTasks(1);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.setInputPaths(job, HDFS_INPUT_DIR);
        FileOutputFormat.setOutputPath(job, HDFS_OUTPUT_DIR);

        boolean ok = job.waitForCompletion(true);
        if (!ok) {
            throw new IllegalStateException("Hadoop MR Job 执行失败");
        }
    }

    private Map<String, Integer> readOutput(FileSystem fs) throws IOException {
        Map<String, Integer> result = new LinkedHashMap<>();

        if (!fs.exists(HDFS_OUTPUT_DIR)) {
            return result;
        }

        FileStatus[] parts = fs.listStatus(HDFS_OUTPUT_DIR, (PathFilter) p -> p.getName().startsWith("part-"));
        for (FileStatus part : parts) {
            try (FSDataInputStream in = fs.open(part.getPath());
                 BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    String[] pieces = line.split("\\t");
                    if (pieces.length != 2) {
                        continue;
                    }
                    String key = pieces[0];
                    int val;
                    try {
                        val = Integer.parseInt(pieces[1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    result.put(key, val);
                }
            }
        }

        return result;
    }
}
