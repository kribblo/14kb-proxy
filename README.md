# 14kb-proxy

One of the optimizations that people look at when doing extreme high performance
web sites is to cram everything that is needed to render an initial view inside the first packet
transferred from the server, including CSS and possibly even some inlined images.

The number usually cited is to aim within the first 14 kilobytes, which gives some margin for headers, variations and overhead
to 16, which is the actual size.

14kb-proxy is a specialized proxy server to help test this.
The browser will simply cut off the traffic after 14 kilobyte for text/html requests and the result can be examined visually as well as
seeing where the cut off happens in the HTML.

Compression is handled properly, cutting off the compressed data at 14 kilobytes - the browser handles unpacking it just fine anyway.

## Usage

You will need Java and Maven.

The proxy comes with a Jetty configuration runnable via Maven, so the simplest way to run the proxy is by the standard command:

	mvn jetty:run

Starts a proxy server on the default port 14336 with the target http://localhost:8080 -
if your site is running there, just visit http://localhost:14336/[URL] instead of the regular one.

	mvn jetty:run -D jetty.port=9999

Starts proxy on port 9999 instead, visit http://localhost:9999

	mvn jetty:run -Dtarget=https://example.com

Starts proxy with custom target, any URL reachable from the machine running on the proxy is fair game.

