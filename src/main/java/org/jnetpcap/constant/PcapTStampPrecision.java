/*
 * Apache License, Version 2.0
 * 
 * Copyright 2013-2022 Sly Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jnetpcap.constant;

import java.util.function.IntSupplier;

/**
 * Time stamp resolution types. Not all systems and interfaces will necessarily
 * support all of these resolutions when doing live captures; all of them can be
 * requested when reading a savefile.
 *
 * <pre>
 * <full>
#define PCAP_TSTAMP_PRECISION_MICRO	0	// use timestamps with microsecond precision, default
#define PCAP_TSTAMP_PRECISION_NANO	1	// use timestamps with nanosecond precision
 * </full>
 * </pre>
 * 
 * @author mark
 *
 */
public enum PcapTStampPrecision implements IntSupplier {

	/** use timestamps with microsecond precision, default */
	TSTAMP_PRECISION_MICRO(1000_000),

	/** use timestamps with nanosecond precision */
	TSTAMP_PRECISION_NANO(1000_000_000),;

	/** use timestamps with microsecond precision, default */
	public static final int PCAP_TSTAMP_PRECISION_MICRO = 0;

	/** use timestamps with nanosecond precision */
	public static final int PCAP_TSTAMP_PRECISION_NANO = 1;

	public static PcapTStampPrecision valueOf(int value) {
		return values()[value];
	}

	private final long scale;

	/**
	 * @param scale the scale of the fractional unit
	 */
	PcapTStampPrecision(long scale) {
		this.scale = scale;
	}

	/**
	 * @see java.util.function.IntSupplier#getAsInt()
	 */
	@Override
	public int getAsInt() {
		return ordinal();
	}

	/**
	 * Converts second and fraction components, as read from pcap header, to milli
	 * seconds in epoch time suitable for use with java {@full Date} class.
	 *
	 * @param epochSeconds     the epoch seconds
	 * @param fractionOfSecond the fraction of second
	 * @return number of millis since start of epoch time
	 */
	public long toEpochMillis(long epochSeconds, long fractionOfSecond) {
		long scaleToMillis = (scale / 1000);

		return toEpochTime(epochSeconds, fractionOfSecond) / scaleToMillis;
	}

	/**
	 * Computes number of seconds in epoch time by dropping the fractional part.
	 *
	 * @param epochTime in this fractional precision, ie. nanos or micros since
	 *                  start of epoch Jan 1st, 1970 12:00am
	 * @return number of seconds in epoch time
	 */
	public long toEpochSeconds(long epochTime) {
		return (epochTime / scale);
	}

	/**
	 * To epoch time with this precision from supplied second and fraction
	 * components, as read fro pcap header.
	 *
	 * @param epochSeconds     the epoch seconds
	 * @param fractionOfSecond the fraction of second in this time unit
	 * @return number of fractional units, in this precision, since start of epoch,
	 *         Jan 1st, 1970 12:00am
	 */
	public long toEpochTime(long epochSeconds, long fractionOfSecond) {
		long timestamp = ((epochSeconds * scale) + fractionOfSecond);

		return timestamp;
	}

	/**
	 * Computes a fraction of a second from epoch time provided
	 *
	 * @param epochTime the epoch time
	 * @return fraction of a second in this precision
	 */
	public long toFractionOfSecond(long epochTime) {
		return (epochTime % scale);
	}
}