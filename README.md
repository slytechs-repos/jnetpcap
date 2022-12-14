# jNetPcap version 2
jNetPcap is a [*libpcap*][libpcap] java binding. This is version 2 release of popular **jNetPcap** library, previously hosted on [SourceForge.net][sf.net].

## Overview
**jNetPcap** provides out of the box [*libpcap* library][libpcap] bindings in *Java*. By using *Foreign Function* features of *Java 19* or above, **jNetPcap** can bind directly to the native *libpcap* library. All native *libpcap* functions are accessible through easy to use *java* API. In addition, the latest [*Npcap*][npcap] and legacy [*WinPcap*][winpcap] tools and their API extensions on *Microsoft Windows* platforms are supported as well. 

## Examples
To get started lets take a look at a couple of examples.

Capturing and transmitting packets is straight forward and easy out of the box. 

> **Note** **jNetPcap** also provides many useful utilities to help in working with the data received, such as byte arrays to hex string, and hex string to byte array, and much more. More advanced utility packet handlers such as no-copy on capture, are provides as well and discussed in the [Wiki pages][wiki]. 

### Capture a Live Packet
This quick example demonstrates how to **capture one or more packets** from a live network.
```java
void main() throws PcapException {
	int PACKET_COUNT = 1;
	List<PcapIf> devices = Pcap.findAllDevs();

	try (Pcap pcap = Pcap.create(devices.get(0))) {
		pcap.activate();

		pcap.loop(PACKET_COUNT, (String msg, PcapHeader header, byte[] packet) -> {

			System.out.println(msg);
			System.out.printf("Packet [timestamp=%s, wirelen=%-4d caplen=%-4d %s]%n",
					Instant.ofEpochMilli(header.toEpochMilli()),
					header.wireLength(),
					header.captureLength(),
					PcapUtils.toHexCurleyString(packet, 0, 6));

		}, "Hello World");
	}
}
```

Which produces the following output:

```
Hello World
Packet [timestamp=2011-03-01T20:45:13.266Z, wirelen=74   caplen=74   {00:26:62:2f:47:87}]
```
### Transmit a Packet With Data-Link Header
This example demonstrates how to **transmit a raw packet** on a live network.

The packet we will transmit looks like this:
```
Frame 1: 74 bytes on wire (592 bits), 74 bytes captured (592 bits)
Ethernet II, Src: ASUSTekC_b3:01:84 (00:1d:60:b3:01:84), Dst: Actionte_2f:47:87 (00:26:62:2f:47:87)
Internet Protocol Version 4, Src: 192.168.253.5, Dst: 192.168.253.6
Transmission Control Protocol, Src Port: 57678 (57678), Dst Port: http (80), Seq: 0, Len: 0
```
We use [Wireshark][wireshark] to convert a previously captured packet to a hex string (*`right click packet -> copy -> "... as a Hex Stream"`*) and then **jNetPcap's** utility method `PcapUtils.parseHexString()` to further convert into a java byte array, which we send as a raw packet:

```java
void main() throws PcapException {

	/* raw bytes of our packet */
	final String ETHERNET = "0026622f4787001d60b301840800";
	final String IPv4 = "4500003ccb5b4000400628e4 c0a8FD05 c0a8FD06";
	final String TCP = "e14e00508e50190100000000a00216d08f470000020405b40402080a0021d25a0000000001030307";
	final byte[] packetBytes = PcapUtils.parseHexString(ETHERNET + IPv4 + TCP);

	List<PcapIf> devices = Pcap.findAllDevs();

	try (Pcap pcap = Pcap.create(devices.get(0))) {
		pcap.activate();

		/* Transmit our packet */
		pcap.sendPacket(packetBytes);
	}
}
```
This example produces no output, but if you monitor the network, you will see our non-routable packet being sent from the example host.

> **Note** `Pcap.inject()` can also be used to transmit packets. We can also transmit data in `ByteBuffer` object, and a foreign native `MemorySegment`, all covered under advanced topics in [wiki].

### Statistics Snapshots
Our last example shows how to **take statistic snapshots** using `Pcap` API. The native *libpcap* library is able to capture statistics, even when our main thread is a sleep and only wakes up to take a snapshot of the current counters, store them in a `PcapStat` record and return them to our main thread. Then we simply print out the statistics structure values, sleep for 1 second and loop once again. For a total of 5 seconds or 5 loops.

```java
void main() throws PcapException, InterruptedException {
	List<PcapIf> devices = Pcap.findAllDevs();

	try (Pcap pcap = Pcap.create(devices.get(0))) {
		pcap.activate();

		long secondsRemaining = 5;

		while (secondsRemaining-- > 0) {
			PcapStat stats = pcap.stats();

			System.out.println(stats);

			TimeUnit.SECONDS.sleep(1);
		}
	}
}
```
Produces the following output:
```
PcapStatRecord[recv=0, drop=0, ifdrop=0, capt=0, sent=0, netdrop=0]
PcapStatRecord[recv=137, drop=0, ifdrop=0, capt=0, sent=0, netdrop=0]
PcapStatRecord[recv=340, drop=0, ifdrop=0, capt=0, sent=0, netdrop=0]
PcapStatRecord[recv=477, drop=0, ifdrop=0, capt=0, sent=0, netdrop=0]
PcapStatRecord[recv=677, drop=0, ifdrop=0, capt=0, sent=0, netdrop=0]
```

### How To Run The Examples
To run these exmamples the following command line arguments need to be added:
<dl><dt>On Linux platforms</dt><dd>-Djava.library.path=/usr/lib/x86_64-linux-gnu --enable-native-access=org.jnetpcap --enable-preview</dd>
<dl><dt>On Windows platforms</dt><dd>-Djava.library.path=C:\Windows\SysWOW64 --enable-native-access=org.jnetpcap --enable-preview</dd>
<dl><dt>On MacOS platforms</dt><dd>-Djava.library.path=/usr/local/Cellar/libpcap/1.10.1/lib --enable-native-access=org.jnetpcap --enable-preview</dd>
	
> **Note** that the `--enable-preview` command line option is only required until *Foreign Function* feature become permanent, possibly in Java 20 or Java 21 LTS.
	
### For more examples
See the [wiki] pages. Project's [unit tests][unit_test] are also a great source for usage examples of every single function in the module.

## Dependencies
**jNetPcap** binding has no external java dependencies except for modules provided by the java runtime.

### Java Dependencies for Module: `org.jnetpcap`
* No java dependencies except for standard java modules and the *Foreign Function* feature, currently in java *preview* (enabled with `--enable-preview` VM args option), but one which is expected to be a permanent feature, in the near future.

### Native Library Dependencies
* The only native dependency is the native [*libpcap* library][libpcap] itself, which has to be installed prior to **jNetPcap** module initializing. On *Microsoft Windows* platforms, install [*WinPcap*][winpcap] or [*Npcap*][npcap] tools instead.

## Installation
Here are several methods for installing **jNetPcap** software.

### Maven Artifact Config
```
<dependency>
    <groupId>org.jnetpcap</groupId>
    <artifactId>jnetpcap</artifactId>
    <version>2.0.0-alpha.1</version>
</dependency>

```
### Download Release Package
> TODO - add a link to the release

### Compile From Source
You will find instructions on how to compile from source on our [Wiki Pages][wiki].

## Documentation
See [Wiki pages][wiki]

## Contact
* `sales@slytechs.com` for commercial and licensing issues
* [jNetPcap Issue Tracker][bugs]

## Compatibility with jNetPcap version 1
There are API and license changes between version 1 and 2 of **jNetPcap**.
Please see [wiki home page][wiki] for details.

[jnetpcap_v1_page]: <https://sourceforge.net/projects/jnetpcap> "Legacy jNetPcap Version 1 Project Page"
[wiki]: <https://github.com/slytechs-repos/jnetpcap/wiki> "jNetPcap Project Wiki Pages"
[unit_test]: <https://github.com/slytechs-repos/jnetpcap/blob/main/src/test/java/org/jnetpcap/test/LibpcapApiTest.java> "jUnit Test of Main Libpcap API bindings"
[libpcap]: <https://www.tcpdump.org/> "This is the home web site of tcpdump, a powerful command-line packet analyzer; and libpcap, a portable C/C++ library for network traffic capture"
[npcap]: <https://npcap.com/> "Npcap is the Nmap Project's packet capture (and sending) library for Microsoft Windows"
[winpcap]: <https://www.winpcap.org/> "WinPcap is a library for link-layer network access in Windows environments"
[wireshark]: <https://wireshark.org> "Wireshark is the world???s foremost and widely-used network protocol analyzer"
[sf.net]: <https://sourceforge.net/projects/jnetpcap/> "jNetPcap version 1 hosted on SourceForge.net"
[bugs]: <https://github.com/slytechs-repos/jnetpcap/issues> "jnetPcap bug reports on Github"

