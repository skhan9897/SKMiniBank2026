package com.bank.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class KafkaShutdownListener implements ServletContextListener {
    @Override public void contextDestroyed(ServletContextEvent event) { KafkaService.close(); }
}
