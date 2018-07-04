package boston.convertdata.repository;

import boston.convertdata.model.structured.Video;
import boston.convertdata.utils.GsonInstances;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Log4j2
public class HdfsRepository extends Configured {

    private final String hdfsBaseUrl;
    private final String hadoopConfDir;

    public HdfsRepository(String hdfsBaseUrl, String hadoopConfDir) {
        this.hdfsBaseUrl = hdfsBaseUrl;
        this.hadoopConfDir = hadoopConfDir;
        Configuration conf = new Configuration();
        if (hadoopConfDir != null && !hadoopConfDir.isEmpty()) {
            val xmlFiles = new File(hadoopConfDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
            for (File xmlFile : xmlFiles) {
                conf.addResource(new Path(xmlFile.getPath()));
            }
        }
    }

    public Video convertHdfsFile2Video(String resultPath) throws IOException {
        // ====== Init HDFS File System Object
        Configuration conf = new Configuration();
        // Set FileSystem URI
        conf.set("fs.defaultFS", hdfsBaseUrl); //hdfsBaseUrl=cdh1:50070
        // Because of Maven
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        // Set HADOOP user
//        System.setProperty("HADOOP_USER_NAME", "hdfs");
//        System.setProperty("hadoop.home.dir", "/");
        System.out.println("configured filesystem = " + conf.get("fs.defaultFS"));
        // Get the filesystem - HDFS
        // FileSystem fs = FileSystem.get(URI.create(hdfsBaseUrl), conf);
        FileSystem fs = FileSystem.get(conf);
        // Read file
        System.out.println("Read file from HDFS...");
        // Create a path, init input stream
        InputStream is = fs.open(new Path(resultPath));

        // Classical input stream usage
        System.out.println("Start to write...");
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, StandardCharsets.UTF_8.name());
        String out = writer.toString();
        System.out.println("result: " + out);
//        String out = IOUtils.toString(is); // "utf-8"
//        System.out.println("Read information: " + out);
        Video video = null;
//        Video video = GsonInstances.ELASTICSEARCH.fromJson(out, Video.class);
//        log.info(video.getVideoId());
//        log.info(video.getSegmentsInfo());
        return video;
//        return GsonInstances.ELASTICSEARCH.fromJson(out, Video.class);
    }

    public static void main(String[] args) {
        try {
            new HdfsRepository("cdh1:8020", "/Users/bingjing/Documents/hadoop-cdh-conf").convertHdfsFile2Video("/user/video/2018-07-03/0da335d3-4ebe-4b3f-a868-c4c494a36c0f.json");
//            new HdfsRepository("cdh1:50070").convertHdfsFile2Video("/user/video/2018-07-03/37ccd554-1311-4b7f-88ae-809f9d3ad839.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
