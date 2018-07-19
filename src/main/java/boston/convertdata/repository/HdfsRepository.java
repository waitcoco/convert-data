package boston.convertdata.repository;

import boston.convertdata.model.structured.Video;
import boston.convertdata.utils.GsonInstances;
import com.google.gson.Gson;
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

//        Path path = new Path("/user/video/2018-07-04/47b549bf-e2d6-4555-9e10-f0a9222c9af3.json");
//        FileSystem fs = path.getFileSystem(conf);
//        FSDataInputStream inputStream = fs.open(path);
//        String theString = IOUtils.toString(inputStream, "UTF-8");
//        Video video = new Gson().fromJson(theString, Video.class);
//        return video;

        // Set FileSystem URI
        // conf.set("fs.defaultFS", hdfsBaseUrl); //hdfsBaseUrl=cdh1:50070
        // log.info("configured filesystem = " + conf.get("fs.defaultFS"));
        // Get the filesystem - HDFS
        FileSystem fs = FileSystem.get(conf);
        // Read file. Create a path, init input stream
        log.info("Read file from HDFS...");
        FSDataInputStream is = fs.open(new Path(resultPath));
        // Classical input stream usage
        log.info("Start to read...");
        String out = IOUtils.toString(is, StandardCharsets.UTF_8);
        return GsonInstances.ELASTICSEARCH.fromJson(out, Video.class);
    }



}
