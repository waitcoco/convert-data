package boston.convertdata.repository;

import boston.convertdata.model.structured.Video;
import boston.convertdata.utils.GsonInstances;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.nio.charset.StandardCharsets;


@Log4j2
public class HdfsRepository extends Configured {

    private final String hdfsBaseUrl;
    private final Configuration conf;

    public HdfsRepository(String hdfsBaseUrl, String hadoopConfDir) {
        this.hdfsBaseUrl = hdfsBaseUrl;
        // ====== Init HDFS File System Object
        conf = new Configuration();
        if (hadoopConfDir != null && !hadoopConfDir.isEmpty()) {
            val xmlFiles = new File(hadoopConfDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
            for (File xmlFile : xmlFiles) {
                conf.addResource(new Path(xmlFile.getPath()));
            }
        }
    }

    public Video convertHdfsFile2Video(String resultPath) throws IOException {
        // Set FileSystem URI
        conf.set("fs.defaultFS", hdfsBaseUrl); //hdfsBaseUrl=cdh1:50070
        log.info("configured filesystem = " + conf.get("fs.defaultFS"));
        // Get the filesystem - HDFS
        FileSystem fs = FileSystem.get(conf);
        // Read file. Create a path, init input stream
        log.info("Read file from HDFS...");
        FSDataInputStream is = fs.open(new Path(resultPath));
        // Classical input stream usage
        log.info("Start to write...");
        String out = IOUtils.toString(is, StandardCharsets.UTF_8);
        return GsonInstances.ELASTICSEARCH.fromJson(out, Video.class);
    }


    /**
     * 扫描文件夹
     *
     * @param fs
     * @param resultPath
     * @throws IOException
     */
    private void listFiles(FileSystem fs, String resultPath) throws IOException {
        System.out.println("list directories...");
        final FileStatus[] listStatus = fs.listStatus(new Path(resultPath));
        for (FileStatus fileStatus : listStatus) {
            String type = fileStatus.isDirectory() ? "Directory" : "File";
            short replication = fileStatus.getReplication();
            String permission = fileStatus.getPermission().toString();
            final long len = fileStatus.getLen();
            Path path = fileStatus.getPath();
            System.out.println(type + "\t" + permission + "\t" + replication + "\t" + len + "\t" + path);
        }
    }

    public static void main(String[] args) {
        try {
            new HdfsRepository("cdh1:8020", "/Users/bingjing/Documents/hadoop-cdh-conf").convertHdfsFile2Video("/user/video/2018-07-04/47b549bf-e2d6-4555-9e10-f0a9222c9af3.json");
            // new HdfsRepository("cdh1:50070").convertHdfsFile2Video("/user/video/2018-07-03/37ccd554-1311-4b7f-88ae-809f9d3ad839.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
