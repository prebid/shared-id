package org.sharedid.endpoint.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Subdivision;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServerRequest;
import org.sharedid.endpoint.util.RequestAddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

public class GeoIpService implements LocationService {
    public static Logger logger = LoggerFactory.getLogger(GeoIpService.class);

    private DatabaseReader countryDatabaseReader;
    private DatabaseReader cityDatabaseReader;
    private WorkerExecutor workerExecutor;

    public GeoIpService(DatabaseReader countryDatabaseReader,
                        DatabaseReader cityDatabaseReader,
                        WorkerExecutor workerExecutor) {
        this.countryDatabaseReader = countryDatabaseReader;
        this.cityDatabaseReader = cityDatabaseReader;
        this.workerExecutor = workerExecutor;
    }

    @Override
    public Future<String> getCountryForAddress(InetAddress address) {
        Promise<String> result = Promise.promise();

        workerExecutor.executeBlocking(promise -> {
            try {
                CountryResponse response = countryDatabaseReader.country(address);
                String countryCode = response.getCountry().getIsoCode();
                promise.complete(countryCode);
            } catch (IOException e) {
                logger.error("IO exception when getting geo from IP", e);
                promise.fail(e);
            } catch (AddressNotFoundException e) {
                logger.debug("Address not found in database {}", address);
                promise.complete(null);
            } catch (GeoIp2Exception e) {
                logger.error("GeoIP exception when getting geo from IP", e);
                promise.fail(e);
            }
        }, false, result);

        return result.future();
    }

    @Override
    public Future<String> getCountryForRequest(HttpServerRequest request) {
        InetAddress address = RequestAddressUtil.getInetAddress(request);
        return getCountryForAddress(address);
    }

    @Override
    public Future<Optional<String>> getStateForAddress(InetAddress address) {
        Promise<Optional<String>> result = Promise.promise();

        workerExecutor.executeBlocking(promise -> {
            try {
                CityResponse response = cityDatabaseReader.city(address);

                Subdivision subdivision = response.getMostSpecificSubdivision();

                if (subdivision == null) {
                    promise.complete(Optional.empty());
                    return;
                }

                String state = subdivision.getIsoCode();

                if (state == null || state.length() == 0) {
                    promise.complete(Optional.empty());
                    return;
                }

                promise.complete(Optional.of(state));
            } catch (IOException e) {
                logger.error("IO exception when getting geo from IP", e);
                promise.fail(e);
            } catch (AddressNotFoundException e) {
                logger.debug("Address not found in database {}", address);
                promise.complete(Optional.empty());
            } catch (GeoIp2Exception e) {
                logger.error("GeoIP exception when getting geo from IP", e);
                promise.fail(e);
            }
        }, false, result);

        return result.future();
    }

    @Override
    public Future<Optional<String>> getStateForRequest(HttpServerRequest request) {
        InetAddress address = RequestAddressUtil.getInetAddress(request);
        return getStateForAddress(address);
    }
}
