package com.urlshortener.config;

import lombok.extern.slf4j.Slf4j;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Slf4j
public class JobRunrConfig {
    @Bean
    public JobScheduler jobScheduler(DataSource dataSource, ApplicationContext applicationContext) {
        int randomPort = isPortAvailable(8000) ? 8000 : generateRandomPort();
        log.info("Configuring JobRunr...");
        JobScheduler scheduler = JobRunr.configure()
                .useJobActivator(applicationContext::getBean)
                .useStorageProvider(SqlStorageProviderFactory
                        .using(dataSource))
                .useBackgroundJobServer()
                .useDashboard(randomPort)
                .initialize()
                .getJobScheduler();
        log.info("JobRunr configured successfully.");
        return scheduler;
    }

    public int generateRandomPort() {
        return ThreadLocalRandom.current().nextInt(1024, 65536);
    }

    // Method to check if a port is available
    public boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;  // Port is available
        } catch (IOException e) {
            return false;  // Port is already in use
        }
    }
}

