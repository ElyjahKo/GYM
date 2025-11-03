package com.gymapp.app.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

public class NetUtil {
    public static String findLocalIp() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            String firstV4 = null;
            while (nics.hasMoreElements()) {
                NetworkInterface ni = nics.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                String name = ni.getName().toLowerCase();
                // Skip typical VPN/virtual adapters
                if (name.contains("vmware") || name.contains("vbox") || name.contains("virtual") || name.contains("tun") || name.contains("tap"))
                    continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                        String ip = a.getHostAddress();
                        if (isPrivateIPv4(ip)) return ip; // best match
                        if (firstV4 == null) firstV4 = ip; // keep as fallback
                    }
                }
            }
            if (firstV4 != null) return firstV4;
        } catch (Exception ignored) {}
        return "localhost";
    }

    public static Set<String> listLocalIps() {
        Set<String> ips = new LinkedHashSet<>();
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface ni = nics.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                String name = ni.getName().toLowerCase();
                if (name.contains("vmware") || name.contains("vbox") || name.contains("virtual") || name.contains("tun") || name.contains("tap"))
                    continue;
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) {
                        String ip = a.getHostAddress();
                        ips.add(ip);
                    }
                }
            }
        } catch (Exception ignored) {}
        if (ips.isEmpty()) ips.add("localhost");
        return ips;
    }

    private static boolean isPrivateIPv4(String ip) {
        return ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\..*\\..*");
    }
}
