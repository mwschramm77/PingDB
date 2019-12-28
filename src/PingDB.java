import oracle.jdbc.OracleTypes;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PingDB {
    public static Integer iterator = 0;
    public static String dbServer = "localhost:1521";
    public static String IP = "8.8.8.8";
    public static String user = "SYSTEM";
    public static String pw = "D1m3ns10n";
    private static boolean rs;
    private static final String CHECK_TABLESPACE =
      "DECLARE "
    + " l_rc sys_refcursor; "
    + " BEGIN "
    + " open l_rc for "
    + " SELECT COUNT(*) FROM DBA_TABLESPACES WHERE TABLESPACE_NAME = 'PINGDB'; "
    + " ? := l_rc;"
    + " END;  ";

    private static final String CREATE_TABLESPACE = " CREATE BIGFILE TABLESPACE PINGDB "
            + "DATAFILE 'H:\\app\\mschramm\\orcl\\product\\12.2.0\\dbhome_1\\database\\PINGDB' "
            + "size 100M AUTOEXTEND ON NEXT 100M MAXSIZE UNLIMITED ";

    private static final String CREATE_USER = "create user PINGDB " +
            "identified by D1m3ns10n default tablespace PINGDB " +
            "QUOTA UNLIMITED ON PINGDB ACCOUNT UNLOCK";

    private static final String GRANT_USER = "grant unlimited tablespace, " +
            "create session, create table, create view, create procedure, " +
            "CREATE TYPE, CREATE SEQUENCE  to PINGDB";

    private static final String CREATE_TABLE = "CREATE TABLE PINGDB.PINGSTATS " +
            "(" +
            "  DATETIME NVARCHAR2(50) " +
            ", IP NVARCHAR2(50) " +
            ", REACHABLE NVARCHAR2(50) " +
            ")";
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static LocalDateTime now = LocalDateTime.now();
//making a change to test git

    public static void getConnection() throws ClassNotFoundException, SQLException {
        String driver = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:thin:@" + dbServer + ":orcl";
        String username = "PINGDB";
        String password = "D1m3ns10n";
        Class.forName(driver);
            Connection conn = DriverManager.getConnection(url, username, password);
    }

    public static Connection getConnection2() throws ClassNotFoundException, SQLException {
        String driver = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:thin:@" + dbServer +":orcl";
        String username = user;
        String password = pw;
        Class.forName(driver);
        Connection conn2 = DriverManager.getConnection(url, username, password);
        return conn2;
    }
    public static void main(String args[]) throws SQLException, ClassNotFoundException {

        if(args.length == 4){
            IP = args[0];
            dbServer = args[1];
            user = args[2];
            pw = args[3];
        }
        if(args.length == 3){
             dbServer = args[0];
            user = args[1];
            pw = args[2];
        }
        if(args.length == 1){
            INIT(args[0]);

        }



        while(true) {
            Connection conn2 = null;
            now = LocalDateTime.now();
            conn2 = getConnection2();
            Statement statement = conn2.createStatement();
            try {
                InetAddress address = InetAddress.getByName(IP);
                // Try to reach the specified address within the timeout
                // period. If during this period the address cannot be
                // reach then the method returns false.
                File myObj = new File("PingTest.txt");
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {
                    //System.out.println("File already exists.");
                }
                boolean reachable = address.isReachable(10000);
                FileWriter myWriter = new FileWriter("PingDB.txt", true);
                System.out.println("" + dtf.format(now) + "\t" + IP + "\t" + reachable);
                statement.executeUpdate("INSERT /*+ MONITOR */ INTO PINGDB.PINGSTATS VALUES ('" + dtf.format(now) + "', '" + IP + "', '" + reachable + "')");
                if (iterator == 0) {
                    myWriter.write("\nProgram Started\n");
                }
                myWriter.write("" + dtf.format(now) + "\t" + IP + "\t" + reachable + "\n");
                myWriter.close();
                if(reachable) {
                    Thread.sleep(60000);
                } else {
                    Thread.sleep(5000);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                conn2.close();
            }
            iterator++;
            conn2.close();
        }


    }

    private static void INIT(String arg) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        CallableStatement cs = null;
        CallableStatement cs2 = null;
        CallableStatement cs3 = null;
        CallableStatement cs4 = null;
        CallableStatement cs5 = null;

        if(arg.equals("INIT")) {
            String driver = "oracle.jdbc.driver.OracleDriver";
            String url = "jdbc:oracle:thin:@" + dbServer + ":orcl";
            String username = user;
            String password = pw;
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
            cs = conn.prepareCall(CHECK_TABLESPACE);
            cs.registerOutParameter(1, OracleTypes.CURSOR);

            //cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            ResultSet cursorResultSet = (ResultSet) cs.getObject(1);

            try {

                while (cursorResultSet.next ())
                {
                    System.out.println (cursorResultSet.getInt(1) );
                    if(cursorResultSet.getInt(1) == 1){
                        System.out.println("Tablespace exists");
                    } else{
                        System.out.println("Creating tablespace...");
                        cs2 = conn.prepareCall(CREATE_TABLESPACE);
                        cs3 = conn.prepareCall(CREATE_USER);
                        cs4 = conn.prepareCall(GRANT_USER);
                        cs5 = conn.prepareCall(CREATE_TABLE);
                        cs2.execute();
                        cs3.execute();
                        cs4.execute();
                        cs5.execute();
                        System.out.println("Tablespace Created, User created, Table Created");
                    }
                }
            } catch (SQLException e) {
                System.out.println("error: failed to create a connection object.");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("other error:");
                e.printStackTrace();
            } finally {
                try {
                    cs.close();
                    conn.close();
                } catch (Exception e) {
                }
            }
        } else{
            IP = "dns.google";
        }
    }
}

