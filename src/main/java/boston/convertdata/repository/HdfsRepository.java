package boston.convertdata.repository;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;


import java.io.File;

@Repository
public class HdfsRepository {
    public HdfsRepository(@Value("${hadoop.conf.dir:}") String hadoopConfDir) {
        Configuration conf = new Configuration();
        if (hadoopConfDir != null && !hadoopConfDir.isEmpty()) {
            val xmlFiles = new File(hadoopConfDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
            for (File xmlFile : xmlFiles) {
                conf.addResource(new Path(xmlFile.getPath()));
            }
        }
    }
}
