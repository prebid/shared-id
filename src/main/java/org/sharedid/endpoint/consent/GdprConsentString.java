package org.sharedid.endpoint.consent;

import com.iabtcf.decoder.DecoderOption;
import com.iabtcf.decoder.TCString;
import com.iabtcf.utils.IntIterable;
import com.iabtcf.v2.RestrictionType;

public class GdprConsentString {
    public static final int PURPOSE_1 = 1;

    private String rawConsentString;
    private TCString tcString;

    public GdprConsentString(String rawConsentString) throws GdprConsentStringException, UnsupportedOperationException {
        this.rawConsentString = rawConsentString;

        try {
            this.tcString = TCString.decode(rawConsentString, DecoderOption.LAZY);
        } catch (UnsupportedOperationException e) {
            throw new GdprConsentStringException("Version is not supported", rawConsentString);
        } catch (Exception e) {
            throw new GdprConsentStringException("Consent string is malformed", rawConsentString);
        }
    }

    public String getRawConsentString() {
        return rawConsentString;
    }

    public boolean isConsentGiven(int vendorId) {
        int version = tcString.getVersion();

        if (version == 1) {
            return isV1ConsentGiven(vendorId);
        }

        if (version == 2) {
            return isV2ConsentGiven(vendorId);
        }

        return true;
    }

    private boolean isV1ConsentGiven(int vendorId) {
        boolean hasPurposeId = tcString.getPurposesConsent().contains(PURPOSE_1);
        boolean hasSharedIdVendorId = tcString.getVendorConsent().contains(vendorId);

        return hasPurposeId && hasSharedIdVendorId;
    }

    private boolean isV2ConsentGiven(int vendorId) {
        boolean isPurposeOneTreatment = tcString.getPurposeOneTreatment();

        if (isPurposeOneTreatment) {
            return false;
        }

        IntIterable vendorConsent = tcString.getVendorConsent();
        IntIterable purposesConsent = tcString.getPurposesConsent();

        boolean hasPurposeOne = purposesConsent.contains(PURPOSE_1);

        if (!hasPurposeOne) {
            return false;
        }

        boolean hasVendorId = vendorConsent.contains(vendorId);

        if (!hasVendorId) {
            return false;
        }

        boolean hasLegitimateInterest = tcString.getVendorLegitimateInterest().contains(vendorId);

        if (!hasLegitimateInterest) {
            return false;
        }

        //Cory (4/6/2020): Ignoring LI transparency fields for now...

        boolean hasPublisherRestrictions = tcString.getPublisherRestrictions().stream().anyMatch(publisherRestriction -> {
            boolean isPurpose1 = publisherRestriction.getPurposeId() == PURPOSE_1;

            if (!isPurpose1) {
                return false;
            }

            boolean isNotAllowed = RestrictionType.NOT_ALLOWED.equals(publisherRestriction.getRestrictionType());
            boolean isForVendor = publisherRestriction.getVendorIds().contains(vendorId);

            if (isNotAllowed && isForVendor) {
                return true;
            }

            return false;
        });

        if (hasPublisherRestrictions) {
            return false;
        }

        return true;
    }
}
