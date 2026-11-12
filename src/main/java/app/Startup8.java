package app;

import jakarta.servlet.*;

public class Startup8 implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    try {
      Database8.initialize();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot initialize QLST8 database", e);
    }
  }
}
