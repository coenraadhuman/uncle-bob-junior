// File: src/test/java/com/example/ratelimit/FakeHttpExchange.java
package com.example.ratelimit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

/** Minimal test double for the JDK-owned {@link HttpExchange} boundary. */
final class FakeHttpExchange extends HttpExchange {

    private final InetSocketAddress remoteAddress;
    private final Headers responseHeaders = new Headers();
    private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    private int responseCode = -1;

    FakeHttpExchange(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    int responseCode() {
        return responseCode;
    }

    Headers responseHeaders() {
        return responseHeaders;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) {
        this.responseCode = rCode;
    }

    @Override
    public OutputStream getResponseBody() {
        return responseBody;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public void close() {
        // no-op: nothing external to release in the fake
    }

    @Override
    public Headers getRequestHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestMethod() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getRequestURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpContext getHttpContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getRequestBody() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpPrincipal getPrincipal() {
        throw new UnsupportedOperationException();
    }
}
