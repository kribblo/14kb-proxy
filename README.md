# 14kb-proxy

One of the optimizations that people look at when doing [extreme high performance web sites](https://www.youtube.com/watch?v=YV1nKLWoARQ&ab_channel=IlyaGrigorik) is to cram everything that is needed to render an initial view inside the first [TCP packet](http://en.wikipedia.org/wiki/Transmission_Control_Protocol) transferred from the server, including CSS and possibly inlined images, fonts and scripts.

The number usually cited is to aim within the first 14 kilobytes, which gives some margin for headers, variations and overhead to 16 kB, which is the usual total size.

*14kb-proxy* is a specialized proxy server to help test this. The browser will simply cut off the traffic after 14 kB for text/html requests and the result can be examined visually as well as seeing where the cut off happens in the HTML.

Compression is handled properly, cutting off the compressed data at 14 kB - the browser streams the unpacking as soon as the bytes arrive. Always test with compression turned on, same as when it will be when going to production.

## Usage

You will need Java and Maven installed.

The proxy comes with a Jetty configuration runnable via Maven, so the simplest way to run the proxy is by the standard command:

	mvn jetty:run

Starts a proxy server on the default port 14336 with the target http://localhost:8080 - if your site is running there, just visit http://localhost:14336/[URL] instead of the regular one.

	mvn jetty:run -D jetty.port=9999

Starts proxy on port 9999 instead, visit http://localhost:9999

	mvn jetty:run -D 14kb.target=https://example.com

Starts proxy with custom target, any URL reachable from the machine running on the proxy is fair game.

## What to use it for

The point about looking at the first 14 kilobytes is to inline stylesheets and possibly images (as data: URIs) and scripts together with enough HTML to render a good enough view of the site within the first packet coming back from the server.

Next best, is to make sure the important styles are linked within that same packet, as early as possible, so that the browser can load those as soon as possible. See [critical rendering path](https://developers.google.com/web/fundamentals/performance/critical-rendering-path/) for more information.

If the site already loads within 14 kilobytes (using compression, remember to always test with it!) then look at what can be inlined and do so until hitting the limit.

If the site is heavier, take a look at what parts arrive before the cap and make sure it's the most important ones. Inlining is still as important here, to avoid hitting the network, if possible.

By simply loading the site via the proxy in a regular browser or [WebPageTest](http://www.webpagetest.org/) it's possible to see what will be rendered using the first 14kB. Looking at the network diagrams or the source will also tell which external resources are starting to download at that point. Look for inlining, or make sure that all are essential.

It can also be used in some automation, perhaps by verifying that a list of matching resources or elements, or some marker is always within the 14 kB. Depends on the site, but maybe you want the whole `<head>` with inlines to fit, or that the main logo or splash screen is included.

## More info

The proxy currently uses [Smiley's HTTP Proxy Servlet](https://github.com/mitre/HTTP-Proxy-Servlet) as the base.

