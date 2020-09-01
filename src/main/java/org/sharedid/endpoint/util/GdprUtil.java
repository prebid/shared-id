package org.sharedid.endpoint.util;

import org.sharedid.endpoint.service.EEACountriesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdprUtil {
    private static final Logger logger = LoggerFactory.getLogger(GdprUtil.class);

    private GdprUtil() {}

    public static boolean isGdprRequired(String countryCode) {
        if (countryCode == null) {
            logger.debug("Location request failed. Defaulting to requiring compliance");
            return true;
        }

        EEACountriesEnum country =
                EEACountriesEnum.getEEACountryByTwoLetterID(countryCode);

        if (country == null) {
            logger.debug("NON-EEA User country {}", countryCode);
            return false;
        }

        if (country.equals(EEACountriesEnum.NON_IDENTIFIABLE_COUNTRY)) {
            logger.debug("Unknown country code {}", countryCode);
            return true;
        }

        logger.debug("EEA country identified {} {}", country.getCountryLabel(), country.getIdCode());

        return true;
    }
}
