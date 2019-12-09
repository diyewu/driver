package xz.driver.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class SortableUUID {

	private static String mac;
	private static int seq;

	static {
		try {
			mac = getMac().get(0);
			seq = 0;
		} catch (Exception e) {
			throw new IllegalStateException(e.getCause());
		}
	}

	public static String randomUUID(Date date) {
		return getTimeMillis(date) + getSeq() + mac;
	}

	public static String randomUUID() {
		return getTimeMillis(null) + getSeq() + mac;
	}

	private static synchronized String getSeq() {
		if (seq >= 99999)
			seq = 0;
		String sseq = String.valueOf(seq++);
		int length = 5 - sseq.length();
		for (int i = 0; i < length; i++) {
			sseq = "0" + sseq;
		}
		return sseq;
	}

	private static String getTimeMillis(Date date) {
		String millis = null;
		if (date != null)
			millis = String.valueOf(date.getTime());
		else
			millis = String.valueOf(System.currentTimeMillis());
		int length = 15 - millis.length();
		for (int i = 0; i < length; i++) {
			millis = "0" + millis;
		}
		return millis;
	}

	private static List<String> getMac() throws SocketException {
		ArrayList<String> macList = new ArrayList<String>();
		Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
		while (nis.hasMoreElements()) {
			NetworkInterface ni = nis.nextElement();
			byte[] mac = ni.getHardwareAddress();
			if (mac == null)
				continue;
			StringBuilder builder = new StringBuilder();
			for (byte b : mac) {
				builder.append(toHexString(b));
			}
			macList.add(builder.toString());
		}
		return macList;
	}

	private static String toHexString(byte b) {
		String ret = Integer.toHexString(0xff & b).toLowerCase();
		if (ret.length() == 1)
			ret = "0" + ret;
		return ret;
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 100; i++) {
			System.out.println(randomUUID());
		}
	}
}
