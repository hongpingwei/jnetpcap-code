/*
 * Copyright (C) 2005, 2006, 2007, 2008, 2009, 2010 Sly Technologies, Inc.
 *
 * This file is part of jNetPcap.
 *
 * jNetPcap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jnetpcap.packet.format;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Formatter;

import org.jnetpcap.packet.JHeader;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.annotate.ProtocolSuite;
import org.jnetpcap.packet.structure.JField;
import org.jnetpcap.protocol.JProtocol.Suite;

// TODO: Auto-generated Javadoc
/**
 * The Class TextFormatter.
 */
public class TextFormatter
    extends JFormatter {

	/** The Constant FIELD_ARRAY_FORMAT. */
	private final static String FIELD_ARRAY_FORMAT = "%16s[%d] = ";

	/** The Constant FIELD_FORMAT. */
	private final static String FIELD_FORMAT = "%16s = ";

	/** The Constant SEPARATOR. */
	private static final String SEPARATOR = ": ";

	/*
	 * Utility Formatter
	 */
	/** The uf. */
	final Formatter uf = new Formatter();

	/**
	 * Instantiates a new text formatter.
	 */
	public TextFormatter() {
	}

	/**
	 * Instantiates a new text formatter.
	 * 
	 * @param out
	 *          the out
	 */
	public TextFormatter(Appendable out) {
		super(out);
	}

	/**
	 * Instantiates a new text formatter.
	 * 
	 * @param out
	 *          the out
	 */
	public TextFormatter(StringBuilder out) {
		super(out);
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.format.JFormatter#fieldAfter(org.jnetpcap.packet.JHeader, org.jnetpcap.packet.structure.JField, org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	protected void fieldAfter(JHeader header, JField field, Detail detail)
	    throws IOException {

		if (field.getStyle() == Style.INT_BITS) {

		} else if (field.hasSubFields()) {
			decLevel();
		} else if (field.getStyle() != Style.BYTE_ARRAY_HEX_DUMP
		    && field.getStyle() != Style.STRING_TEXT_DUMP) {
			decLevel();
		}
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.format.JFormatter#fieldBefore(org.jnetpcap.packet.JHeader, org.jnetpcap.packet.structure.JField, org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	protected void fieldBefore(JHeader header, JField field, Detail detail)
	    throws IOException {

		if (field.hasSubFields()) {
			final String v = stylizeSingleLine(header, field, field.getValue(header));
			pad().format(FIELD_FORMAT + "%s", field.getDisplay(header), v);
			incLevel(19);

		} else if (field.getStyle() == Style.INT_BITS) {

			final String v = stylizeSingleLine(header, field, field.getValue(header));
			final String d = field.getValueDescription(header);
			final long i = field.longValue(header);
			pad().format("%s = [%d] %s%s", v, i, field.getDisplay(header),
			    ((d == null) ? "" : ": " + d));

		} else if (field.getStyle() == Style.BYTE_ARRAY_HEX_DUMP
		    || field.getStyle() == Style.STRING_TEXT_DUMP) {
			decLevel();
			decLevel();
			final String[] v =
			    stylizeMultiLine(header, field, field.getValue(header));
			for (String i : v) {
				pad().format("%s", i);
			}

		} else if (field.getStyle() == Style.BYTE_ARRAY_ARRAY_IP4_ADDRESS) {
			byte[][] table = (byte[][]) field.getValue(header);

			int i = 0;
			for (byte[] b : table) {
				final String v = stylizeSingleLine(header, field, b);
				pad().format(FIELD_ARRAY_FORMAT + "%s", field.getDisplay(header), i++,
				    v);
			}

			incLevel(0); // Inc for multi line fields
		} else if (field.getStyle() == Style.STRING_ARRAY) {
			String[] table = (String[]) field.getValue(header);

			int i = 0;
			for (String b : table) {
				pad().format(FIELD_ARRAY_FORMAT + "%s", field.getDisplay(header), i++,
				    b);
			}

			incLevel(0); // Inc for multi line fields
		} else {

			final String v = stylizeSingleLine(header, field, field.getValue(header));
			final String description = field.getValueDescription(header);
			final String units = field.getUnits(header);

			pad().format(FIELD_FORMAT + "%s", field.getDisplay(header), v);

			if (units != null) {
				out.format(" " + units);
			}

			if (description != null) {
				out.format(" [" + description + "]");
			}

			incLevel(19); // Inc for multi line fields

		}

	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.format.JFormatter#headerAfter(org.jnetpcap.packet.JHeader, org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	protected void headerAfter(JHeader header, Detail detail) throws IOException {
		pad();

		decLevel();
		decLevel();
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.format.JFormatter#headerBefore(org.jnetpcap.packet.JHeader, org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	protected void headerBefore(JHeader header, Detail detail) throws IOException {

		final String name = header.getNicname();
		incLevel(name);
		incLevel(SEPARATOR);

		Suite suite = header.getAnnotatedHeader().getSuite();
		String suiteStr = "";
		if (suite != ProtocolSuite.OTHER) {
			suiteStr = "protocol suite=" + suite.name().replace('_', '/');
		}

		if (header.hasDescription()) {
			pad().format(" ******* %s - \"%s\" - offset=%d (0x%X) length=%d %s",
			    header.getName(), header.getDescription(), header.getOffset(),
			    header.getOffset(), header.getLength(), suiteStr);
		} else {
			pad().format(" ******* %s offset=%d (0x%X) length=%d %s",
			    header.getName(), header.getOffset(), header.getOffset(),
			    header.getLength(), suiteStr);
		}
		pad();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnetpcap.packet.format.JFormatter#packetAfter(org.jnetpcap.packet.JPacket,
	 *      org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	@Override
	public void packetAfter(JPacket packet, Detail detail) throws IOException {
		if (frameIndex != -1) {
			pad().format("END OF PACKET %d", frameIndex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnetpcap.packet.format.JFormatter#packetBefore(org.jnetpcap.packet.JPacket,
	 *      org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	@Override
	public void packetBefore(JPacket packet, Detail detail) throws IOException {
		incLevel("Frame:");
		pad();
		if (frameIndex != -1) {
			pad().format(FIELD_FORMAT + "%d", "#", frameIndex);
		} else {
			pad().format(FIELD_FORMAT + "%d", "number",
			    packet.getState().getFrameNumber());

		}

		pad()
		    .format(
		        FIELD_FORMAT + "%s",
		        "timestamp",
		        new Timestamp(packet.getCaptureHeader().timestampInMillis())
		            .toString());

		pad().format(FIELD_FORMAT + "%d bytes", "wire length",
		    packet.getCaptureHeader().wirelen());
		pad().format(FIELD_FORMAT + "%d bytes", "captured length",
		    packet.getCaptureHeader().caplen());

		pad();
		decLevel();
	}

	/* (non-Javadoc)
	 * @see org.jnetpcap.packet.format.JFormatter#packetNull(org.jnetpcap.packet.JPacket, org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	@Override
	protected void packetNull(JPacket packet, Detail detail) {
		pad().format("packet: NULL");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnetpcap.packet.format.JFormatter#subHeaderAfter(org.jnetpcap.packet.JHeader,
	 *      org.jnetpcap.packet.JHeader,
	 *      org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	@Override
	protected void subHeaderAfter(JHeader header, JHeader subHeader, Detail detail)
	    throws IOException {

		// decLevel();
		// decLevel();
		//		
		// incLevel(SEPARATOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnetpcap.packet.format.JFormatter#subHeaderBefore(org.jnetpcap.packet.JHeader,
	 *      org.jnetpcap.packet.JHeader,
	 *      org.jnetpcap.packet.format.JFormatter.Detail)
	 */
	@Override
	protected void subHeaderBefore(
	    JHeader header,
	    JHeader subHeader,
	    Detail detail) throws IOException {
		pad();
		// decLevel();
		//		
		// incLevel(":" + subHeader.getNicname());
		// incLevel(SEPARATOR);

		pad().format("+ %s: offset=%d length=%d", subHeader.getName(),
		    subHeader.getOffset(), subHeader.getLength());
	}
}
