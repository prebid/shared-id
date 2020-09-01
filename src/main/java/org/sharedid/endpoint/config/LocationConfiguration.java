package org.sharedid.endpoint.config;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import org.sharedid.endpoint.service.GeoIpService;
import org.sharedid.endpoint.service.LocationService;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class LocationConfiguration {
    @Bean
    WorkerExecutor locationWorkerExecutor(
            Vertx vertx,
            @Value("${worker-executor.location.name}") String name,
            @Value("${worker-executor.location.pool-size}") int poolSize,
            @Value("${worker-executor.location.max-execute-time.millis}") long maxExecuteTimeMillis) {
        return vertx.createSharedWorkerExecutor(name, poolSize, maxExecuteTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Bean
    LocationService locationService(WorkerExecutor locationWorkerExecutor,
                                    @Value("${service.geoip.country-database-file}") String countryGeoDatabaseFile,
                                    @Value("${service.geoip.city-database-file}") String cityGeoDatabaseFile) {
        try {
            DatabaseReader countryDatabaseReader =
                    new DatabaseReader.Builder(new File(countryGeoDatabaseFile))
                            .withCache(new CHMCache())
                            .build();

            DatabaseReader cityDatabaseReader =
                    new DatabaseReader.Builder(new File(cityGeoDatabaseFile))
                        .withCache(new CHMCache())
                        .build();

            return new GeoIpService(countryDatabaseReader, cityDatabaseReader, locationWorkerExecutor);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open geo database", e);
        }
    }
}
