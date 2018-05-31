package boston.convertdata;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DriverManager;

public class HiveHelper {
    public static final String name ="org.apache.hive.jdbc.HiveDriver";
    public static final String url = "jdbc:hive2://spark0:10000/boston";

    public Connection connection = null;
    public Statement statement = null;

    public HiveHelper(){
        try{
            Class.forName(name);
            connection = DriverManager.getConnection(url,"hive","");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            this.connection.close();
            this.statement.close();
        }
        catch(SQLException e){
            System.out.println("关闭数据库出现问题！");
            e.printStackTrace();
        }
    }

    public boolean executedNoquery(String sql){
        boolean flag = false;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
            flag = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}


