package org.sharedid.endpoint.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum EEACountriesEnum
{
    //https://wiki.rubiconproject.com/display/~strasler@rubiconproject.com/GDPR+Consent+Tech+Spec#GDPRConsentTechSpec-MembercountriesandterritoriesoftheEEA
    NON_IDENTIFIABLE_COUNTRY("NON", "INVALID_COUNTRY_FORMAT"),

    //EU Members
    AUSTRIA_ID("AT", "AUSTRIA"),
    BULGARIA_ID("BG", "BULGARIA"),
    BELGIUM_ID("BE", "BELGIUM"),
    CYPRUS_ID("CY", "CYPRUS"),
    CZECH_REPUBLIC_ID("CZ", "CZECH_REPUBLIC"),
    DENMARK_ID("DK", "DENMARK"),
    ESTONIA_ID("EE", "ESTONIA"),
    FINLAND_ID("FI", "FINLAND"),
    FRANCE_ID("FR", "FRANCE"),
    GERMANY_ID("DE", "GERMANY"),
    GREECE_ID("GR", "GREECE"),
    HUNGARY_ID("HU", "HUNGARY"),
    IRELAND_ID("IE", "IRELAND"),
    ITALY_ID("IT", "ITALY"),
    LATVIA_ID("LV", "LATVIA"),
    LITHUANIA_ID("LT", "LITHUANIA"),
    LUXEMBOURG_ID("LU", "LUXEMBOURG"),
    MALTA_ID("MT", "MALTA"),
    NETHERLANDS_ID("NL", "NETHERLANDS"),
    POLAND_ID("PL", "POLAND"),
    PORTUGAL_ID("PT", "PORTUGAL"),
    ROMANIA_ID("RO", "ROMANIA"),
    SLOVAKIA_ID("SK", "SLOVAKIA"),
    SLOVENIA_ID("SI", "SLOVENIA"),
    SPAIN_ID("ES", "SPAIN"),
    SWEDEN_ID("SE", "SWEDEN"),
    GREAT_BRITAIN("GB", "UNITED_KINGDOM"),
    GREAT_BRITAIN_UK("UK", "UNITED_KINGDOM_NON_STANDARD_ID"),

    //NON-EU Members within EEA
    ICELAND_ID("IS", "ICELAND"),
    NORWAY_ID("NO", "NORWAY"),
    LIECHTENSTEIN_ID("LI", "LIECHTENSTEIN"),

    //EEA overseas countries and territories
    ANGUILLA_ID("AI", "ANGUILLA"),
    ARUBA_ID("AW", "ARUBA"),
    AZORES_ID("PT", "AZORES"),
    BERMUDA_ID("BM", "BERMUDA"),
    BRITISH_ANTARCTIC_TERRITORY_ID("AQ", "BRITISH_ANTARCTIC_TERRITORY"),
    BRITISH_INDIAN_OCEAN_TERRITORY_ID("IO", "BRITISH_INDIAN_OCEAN_TERRITORY"),
    BRITISH_VIRGIN_ISLANDS_ID("VG", "BRITISH_VIRGIN_ISLANDS"),
    CANARY_ISLANDS_ID("IC", "CANARY_ISLANDS"),
    CAYMAN_ISLANDS_ID("KY", "CAYMAN_ISLANDS"),
    FALKLAND_ISLANDS_ID("FK", "FALKLAND_ISLANDS"),
    FRENCH_OVERSEAS_DEPARTMENTS_RE_ID("RE", "FRENCH_OVERSEAS_DEPARTMENTS_RE"),
    FRENCH_OVERSEAS_DEPARTMENTS_MQ_ID("MQ", "FRENCH_OVERSEAS_DEPARTMENTS_MQ"),
    FRENCH_OVERSEAS_DEPARTMENTS_GP_ID("GP", "FRENCH_OVERSEAS_DEPARTMENTS_GP"),
    FRENCH_OVERSEAS_DEPARTMENTS_GF_ID("GF", "FRENCH_OVERSEAS_DEPARTMENTS_GF"),
    FRENCH_OVERSEAS_DEPARTMENTS_YT_ID("YT", "FRENCH_OVERSEAS_DEPARTMENTS_YT"),
    FRENCH_POLYNESIA_ID("PF", "FRENCH_POLYNESIA"),
    FRENCH_SOUTHERN_AND_ARCTIC_ID("TF", "FRENCH_SOUTHERN_AND_ARCTIC"),
    GREENLAND_ID("GL", "GREENLAND"),
    MADEIRA_ID("PT", "MADEIRA"),
    MONTSERRAT("MS", "MONTSERRAT"),
    NETHERLANDS_ANTILLES_ID("AN", "NETHERLANDS_ANTILLES"),
    NETHERLANDS_ANTILLES_BQ_ID("BQ", "NETHERLANDS_ANTILLES_BQ"),
    NETHERLANDS_ANTILLES_CW_ID("CW", "NETHERLANDS_ANTILLES_CW"),
    NETHERLANDS_ANTILLES_SX_ID("SX", "NETHERLANDS_ANTILLES_SX"),
    NEW_CALEDONIA_ID("NC", "NEW_CALEDONIA_ID"),
    PITCAIRN_ID("PN", "PITCAIRN"),
    SAINT_HELENA_ID("SH", "SAINT_HELENA"),
    SOUTH_GEORGIA_AND_SOUTH_SANDWICH_IS_ID("GS", "SOUTH_GEORGIA_AND_SOUTH_SANDWICH_IS"),
    TURKS_AND_CACAOS_ID("TC", "TURKS_AND_CACAOS_ID"),
    WALLIS_AND_FUTUNA_IS_ID("WF", "WALLIS_AND_FUTUNA_IS");

    private String idCode;
    private String countryLabel;
    private static Pattern p = Pattern.compile("[^a-zA-Z]");

    private static final Map<String, EEACountriesEnum> EEAAbreviatedMap = new HashMap();

    EEACountriesEnum(String idCode, String countryLabel)
    {
        this.idCode = idCode;
        this.countryLabel = countryLabel;
    }

    public String getIdCode()
    {
        return idCode;
    }

    public String getCountryLabel()
    {
        return countryLabel;
    }

    public static EEACountriesEnum getEEACountryByTwoLetterID(String id)
    {

        if(id == null || id.length() != 2 || p.matcher(id).find())
        {
            return EEACountriesEnum.NON_IDENTIFIABLE_COUNTRY;
        }

        EEACountriesEnum e = (EEACountriesEnum)EEAAbreviatedMap.get(id.toUpperCase());
        return e;
    }

    static {
        EEACountriesEnum[] arr$ = values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            EEACountriesEnum e = arr$[i$];
            EEAAbreviatedMap.put(e.getIdCode(), e);
        }

    }
}
