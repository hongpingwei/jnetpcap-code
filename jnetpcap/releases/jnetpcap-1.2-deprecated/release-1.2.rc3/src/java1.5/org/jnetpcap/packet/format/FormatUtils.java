/**
 * Copyright (C) 2008 Sly Technologies, Inc. This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.jnetpcap.packet.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Various static formatting utilities
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class FormatUtils {

	private final static List<String> multiLineStringList =
	    new ArrayList<String>();

	private static final String SPACE_CHAR = " ";

	static String[] table = new String[256];

	static {

		for (int i = 0; i < 31; i++) {
			table[i] = "\\" + Integer.toHexString(i);
			if (table[i].length() == 2)
				table[i] += " ";
		}

		for (int i = 31; i < 127; i++)
			table[i] = new String(new byte[] {
			    (byte) i,
			    ' ',
			    ' ' });

		for (int i = 127; i < 256; i++) {
			table[i] = "\\" + Integer.toHexString(i);
			if (table[i].length() == 2)
				table[i] += " ";
		}

		table[0] = "\\0 ";
		table[7] = "\\a ";
		table[11] = "\\v ";
		table['\b'] = "\\b ";
		table['\t'] = "\\t ";
		table['\n'] = "\\n ";
		table['\f'] = "\\f ";
		table['\r'] = "\\r ";
	}

	/**
	 * Converts the given byte array to a string using a default separator
	 * character
	 * 
	 * @param array
	 *          array to convert
	 * @return the converted string
	 */
	@SuppressWarnings("unused")
	public static String asString(byte[] array) {
		return asString(array, ':');
	}

	/**
	 * Convers the given byte array to a string using the supplied separator
	 * character
	 * 
	 * @param array
	 *          array to convert
	 * @param separator
	 *          separator character to use in between array elements
	 * @return the converted string
	 */
	public static String asString(byte[] array, char separator) {
		StringBuilder buf = new StringBuilder();
		for (byte b : array) {
			if (buf.length() != 0) {
				buf.append(separator);
			}

			if (b >= 0 && b < 16) {
				buf.append('0');
			}

			buf.append(Integer.toHexString((b < 0) ? b + 256 : b).toUpperCase());
		}

		return buf.toString();
	}

	/**
	 * Converts the given byte array to a string using the supplied separator
	 * character and radix for conversion of the numerical component
	 * 
	 * @param array
	 *          array to convert
	 * @param separator
	 *          separator character to use in between array elements
	 * @param radix
	 *          numerical radix to use for numbers
	 * @return the converted string
	 */
	public static String asString(byte[] array, char separator, int radix) {
		StringBuilder buf = new StringBuilder();
		for (byte b : array) {
			if (buf.length() != 0) {
				buf.append(separator);
			}
			if (radix == 16) {
				buf.append(Integer.toHexString((b < 0) ? b + 256 : b).toUpperCase());
			} else {
				buf.append(Integer.toString((b < 0) ? b + 256 : b).toUpperCase());

			}
		}

		return buf.toString();
	}

	/**
	 * Handles various forms of ip6 addressing
	 * 
	 * <pre>
	 *  2001:0db8:0000:0000:0000:0000:1428:57ab
	 * 	2001:0db8:0000:0000:0000::1428:57ab
	 * 	2001:0db8:0:0:0:0:1428:57ab
	 * 	2001:0db8:0:0::1428:57ab
	 * 	2001:0db8::1428:57ab
	 * 	2001:db8::1428:57ab
	 * </pre>
	 * 
	 * @param array
	 *          address array
	 * @param holes
	 *          if true holes are allowed
	 * @return formatted string
	 */
	public static String asStringIp6(byte[] array, boolean holes) {
		StringBuilder buf = new StringBuilder();

		int len = 0;
		int start = -1;
		/*
		 * Check for byte compression where sequential zeros are replaced with ::
		 */
		for (int i = 0; i < array.length && holes; i++) {
			if (array[i] == 0) {
				if (len == 0) {
					start = i;
				}

				len++;
			}

			/*
			 * Only the first sequence of 0s is compressed, so break out
			 */
			if (array[i] != 0 && len != 0) {
				break;
			}
		}

		/*
		 * Now round off to even length so that only pairs are compressed
		 */
		if (start != -1 && (start % 2) == 1) {
			start++;
			len--;
		}

		if (start != -1 && (len % 2) == 1) {
			len--;
		}

		for (int i = 0; i < array.length; i++) {
			if (i == start) {
				buf.append(':');
				i += len - 1;

				if (i == array.length - 1) {
					buf.append(':');
				}
				continue;
			}

			byte b = array[i];

			if (buf.length() != 0 && (i % 2) == 0) {
				buf.append(':');
			}
			if (b < 16) {
				buf.append('0');
			}
			buf.append(Integer.toHexString((b < 0) ? b + 256 : b).toUpperCase());
		}

		return buf.toString();
	}

	/**
	 * Converts the byte arra to hexdump string
	 * 
	 * @param array
	 *          array to convert
	 * @param addressOffset
	 *          offset of the address space reported
	 * @param dataOffset
	 *          offset of the data space reported
	 * @param doAddress
	 *          flag which specifies if address should be printed
	 * @param doText
	 *          flag which specifies if text should printed
	 * @param doData
	 *          flag which specifies if data should printed
	 * @return converted string
	 */
	public static String hexdumpCombined(byte[] array, int addressOffset,
	    int dataOffset, boolean doAddress, boolean doText, boolean doData) {
		StringBuilder b = new StringBuilder();
		for (String s : hexdump(array, addressOffset, dataOffset, doAddress,
		    doText, doData)) {
			b.append(s).append('\n');
		}

		return b.toString();
	}

	/**
	 * Formats a byte array to a hexdump string
	 * 
	 * @param array
	 *          array to convert
	 * @param addressOffset
	 *          offset of the address space reported
	 * @param dataOffset
	 *          offset of the data space reported
	 * @param doAddress
	 *          flag which specifies if address should be printed
	 * @param doText
	 *          flag which specifies if text should printed
	 * @param doData
	 *          flag which specifies if data should printed
	 * @return converted string array, one array element per line of output
	 */
	public static String[] hexdump(byte[] array, int addressOffset,
	    int dataOffset, boolean doAddress, boolean doText, boolean doData) {

		multiLineStringList.clear();

		for (int i = 0; i + dataOffset < array.length; i += 16) {
			multiLineStringList.add(hexLine(array, i + addressOffset, i + dataOffset,
			    doAddress, doText, doData));
		}

		return multiLineStringList.toArray(new String[multiLineStringList.size()]);
	}

	/**
	 * Converts the byte arra to hexdump string
	 * 
	 * @param array
	 *          array to convert
	 * @param addressOffset
	 *          offset of the address space reported
	 * @param dataOffset
	 *          offset of the data space reported
	 * @param doAddress
	 *          flag which specifies if address should be printed
	 * @param doText
	 *          flag which specifies if text should printed
	 * @param doData
	 *          flag which specifies if data should printed
	 * @return converted string array, one array element per line of output
	 */
	public static String hexLine(byte[] array, int addressOffset, int dataOffset,
	    boolean doAddress, boolean doText, boolean doData) {
		String s = "";
		if (doAddress) {
			s += hexLineAddress(addressOffset);
			s += ":" + SPACE_CHAR;
		}

		if (doData) {
			s += hexLineData(array, dataOffset);
		}

		if (doText) {
			s += SPACE_CHAR;
			s += SPACE_CHAR;
			s += SPACE_CHAR;

			s += hexLineText(array, dataOffset);
		}

		return (s);
	}

	/**
	 * Format an address
	 * 
	 * @param address
	 *          integer address
	 * @return formatted address string
	 */
	public static String hexLineAddress(int address) {
		String s = "";

		s = Integer.toHexString(address);

		for (int i = s.length(); i < 4; i++)
			s = "0" + s;

		return (s);
	}

	/**
	 * Formats the data array as a hexdump
	 * 
	 * @param data
	 *          data array
	 * @param offset
	 *          offset into the array
	 * @return formatted string
	 */
	public static String hexLineData(byte[] data, int offset) {
		String s = "";

		int i = 0;
		for (i = 0; i + offset < data.length && i < 16; i++) {

			/**
			 * Insert a space every 4 characaters.
			 */
			if (i % 4 == 0 && i != 0)
				s += SPACE_CHAR;

			s += toHexString(data[i + offset]);
		}

		/**
		 * Continue the loop and append spaces to fill in the missing data.
		 */
		for (; i < 16; i++) {
			/**
			 * Insert a space every 4 characaters.
			 */
			if (i % 4 == 0 && i != 0)
				s += SPACE_CHAR;

			s += SPACE_CHAR + SPACE_CHAR;
		}

		return (s);
	}

	/**
	 * Formats the array data to human readable text that appears at the end of a
	 * hexline of a hexdump
	 * 
	 * @param data
	 *          data array
	 * @param offset
	 *          offset into data array
	 * @return formatted string
	 */
	public static String hexLineText(byte[] data, int offset) {

		String s = "";

		int i;
		for (i = 0; i + offset < data.length && i < 16; i++) {
			s += table[data[i + offset] & 0xFF];

			// if(Character.isLetterOrDigit(table[data[i + offset] & 0xFF]) ||
			// (table[data[i + offset] & 0xFF]) == ' ')
			// s += " " + table[data[i + offset] & 0xFF];
			// else
			// s += " " + NONPRINTABLE_CHAR;
		}

		/**
		 * Continue the loop and fill in any missing data less than 16 bytes.
		 */
		for (; i < 16; i++) {
			s += SPACE_CHAR;
		}

		return (s);
	}

	/**
	 * Parses a string containing hex numbers to a byte array
	 * 
	 * @param source
	 *          source string
	 * @return byte array from the parsed data
	 */
	public static byte[] toByteArray(String source) {

		String s = source.replaceAll(" |\n", "");

		byte[] b = new byte[s.length() / 2];

		if ((s.length() % 2) != 0) {
			System.err.println(s);
			throw new IllegalArgumentException(
			    "need even number of hex double digits [" + s.length() + "]");
		}

		for (int i = 0; i < s.length(); i += 2) {
			String q = s.substring(i, i + 2);
			// System.out.print(q);
			b[i / 2] = (byte) Integer.parseInt(q, 16);
		}

		return b;
	}

	/**
	 * Formats a number to hext
	 * 
	 * @param b
	 *          input byte
	 * @return formatted string
	 */
	public static String toHexString(byte b) {
		String s = Integer.toHexString(((int) b) & 0xFF);

		if (s.length() == 1)
			return ("0" + s);

		return (s);
	}

	/*
	 * Few constants to help with breakdown of millis to various larger units of
	 * time
	 */
	private final static int SECOND_MILLIS = 1000;

	private final static int MINUTE_MILLIS = SECOND_MILLIS * 60;

	private final static int HOUR_MILLIS = MINUTE_MILLIS * 60;

	private final static int DAY_MILLIS = HOUR_MILLIS * 24;

	private final static int WEEK_MILLIS = DAY_MILLIS * 7;

	/**
	 * Formats a delta time
	 * 
	 * @param millis
	 *          delta timestamp in millis
	 * @return formatted string
	 */
	public static String formatTimeInMillis(long millis) {

		StringBuilder b = new StringBuilder();

		long u = 0;

		while (millis > 0) {
			if (b.length() != 0) {
				b.append(' ');
			}

			if (millis > WEEK_MILLIS) {
				u = millis / WEEK_MILLIS;
				b.append(u).append(' ').append((u > 1) ? "weeks" : "week");
				millis -= u * WEEK_MILLIS;

			} else if (millis > DAY_MILLIS) {
				u = millis / DAY_MILLIS;
				b.append(u).append(' ').append((u > 1) ? "days" : "day");
				millis -= u * DAY_MILLIS;

			} else if (millis > HOUR_MILLIS) {
				u = millis / HOUR_MILLIS;
				b.append(u).append(' ').append((u > 1) ? "days" : "day");
				millis -= u * HOUR_MILLIS;

			} else if (millis > MINUTE_MILLIS) {
				u = millis / MINUTE_MILLIS;
				b.append(u).append(' ').append((u > 1) ? "minutes" : "minute");
				millis -= u * MINUTE_MILLIS;

			} else if (millis > SECOND_MILLIS) {
				u = millis / SECOND_MILLIS;
				b.append(u).append(' ').append((u > 1) ? "seconds" : "second");
				millis -= u * SECOND_MILLIS;

			} else if (millis > 0) {
				u = millis;
				b.append(u).append(' ').append((u > 1) ? "millis" : "milli");
				millis -= u;
			}
		}

		return b.toString();
	}
}
