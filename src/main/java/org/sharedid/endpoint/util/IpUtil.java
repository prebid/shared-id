package org.sharedid.endpoint.util;

/*
    Simple util for converting ip addresses from and to integer
    format.
 */
public class IpUtil {
    //converts ip address in integer format to string format
    public static String intToIp(int ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    public static int ipToInt(String ip) throws IpUtilException, NumberFormatException {
        String[] addr = ip.split("\\.");

        if (addr.length != 4) {
            throw new IpUtilException();
        }

        long num = 0;

        for (int i = 0; i < addr.length; i++) {
            int power = 3 - i;
            num += (Integer.parseInt(addr[i]) % 256 * Math.pow(256, power));
        }

        return (int) num;
    }
}
