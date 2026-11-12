package app;

import dao.DAO8;
import java.io.File;
import org.apache.catalina.startup.Tomcat;

public class Main8 {
  public static void main(String[] args) throws Exception {
    Tomcat server = new Tomcat();
    server.setBaseDir("target/tomcat");
    server.setHostname("127.0.0.1");
    server.setPort(Integer.parseInt(DAO8.setting("QLST_PORT", "8080")));
    server.getConnector().setProperty("address", "127.0.0.1");
    var context = server.addWebapp("", new File("src/main/webapp").getAbsolutePath());
    context.setParentClassLoader(Main8.class.getClassLoader());
    server.start();
    System.out.println(
        "QLST8: http://127.0.0.1:" + server.getConnector().getLocalPort() + "/app/home");
    server.getServer().await();
  }
}
