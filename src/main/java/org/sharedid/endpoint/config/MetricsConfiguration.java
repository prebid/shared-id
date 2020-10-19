package org.sharedid.endpoint.config;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.github.azagniotov.metrics.reporter.cloudwatch.CloudWatchReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfiguration {
    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    @ConditionalOnProperty("metrics.cloud-watch.enabled")
    public CloudWatchAsyncClient cloudWatchAsyncClient(@Value("${aws.region}") String awsRegion) {
        return CloudWatchAsyncClient
                .builder()
                .region(Region.of(awsRegion))
                .build();
    }

    @Bean
    @ConditionalOnProperty("metrics.cloud-watch.enabled")
    public CloudWatchReporter cloudWatchReporter(MetricRegistry metricRegistry,
                                                 CloudWatchAsyncClient cloudWatchAsyncClient,
                                                 @Value("${metrics.cloud-watch.namespace}") String namespace) {
        CloudWatchReporter reporter =
                CloudWatchReporter.forRegistry(metricRegistry, cloudWatchAsyncClient, namespace)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .withPercentiles(CloudWatchReporter.Percentile.P75, CloudWatchReporter.Percentile.P99)
                    .withOneMinuteMeanRate()
                    .withFiveMinuteMeanRate()
                    .withFifteenMinuteMeanRate()
                    .withMeanRate()
                    .withArithmeticMean()
                    .withStdDev()
                    .withStatisticSet()
                    .withZeroValuesSubmission()
                    .withReportRawCountValue()
                    .withHighResolution()
                    .withMeterUnitSentToCW(StandardUnit.BYTES)
                    .withJvmMetrics()
                    .build();

        reporter.start(1, TimeUnit.MINUTES);

        return reporter;
    }
}
