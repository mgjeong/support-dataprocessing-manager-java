/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.websocket;

import org.edgexfoundry.support.dataprocessing.runtime.engine.flink.connectors.websocket.WebSocketServerSink;
import org.edgexfoundry.support.dataprocessing.runtime.task.DataSet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class WebSocketServerSinkTest {
    private static final int TEMP_PORT = 8865;

    @Test
    public void testWebSocketOpenClose() throws Exception {
        WebSocketServerSink sink = new WebSocketServerSink(TEMP_PORT);
        try {
            sink.open(null);
        } finally {
            sink.close();
        }
    }

    @Test
    public void testInvalidWebSocketVersion() throws Exception {
        WebSocketServerSink sink = new WebSocketServerSink(TEMP_PORT);
        WebClient clientA = new WebClient(TEMP_PORT, WebSocketVersion.UNKNOWN);
        try {
            sink.open(null);
            try {
                clientA.open();
                Assert.fail("Should not reach here.");
            } catch (WebSocketHandshakeException e) {
                // Should reach here.
            }
        } finally {
            if (clientA != null) {
                clientA.close();
            }
            sink.close();
        }
    }

    @Test
    public void testWebSocketSingleClient() throws Exception {
        WebSocketServerSink sink = new WebSocketServerSink(TEMP_PORT);
        WebClient clientA = new WebClient(TEMP_PORT);
        try {
            sink.open(null);
            clientA.open();
            sink.invoke(DataSet.create("{}"));
        } finally {
            clientA.close();
            sink.close();
        }
    }

    @Test
    public void testWebSocketMultiClient() throws Exception {
        WebSocketServerSink sink = new WebSocketServerSink(TEMP_PORT);
        WebClient clientA = new WebClient(TEMP_PORT);
        WebClient clientB = new WebClient(TEMP_PORT);
        try {
            sink.open(null);
            clientA.open();
            clientB.open();
            clientA.write();
            sink.invoke(DataSet.create("{}"));
            clientA.close();
            clientA = null;
        } finally {
            if (clientA != null) {
                clientA.close();
            }
            clientB.close();
            sink.close();
        }
    }

    private static class WebClient {
        private final URI uri;
        private Channel clientChannel;
        private final EventLoopGroup group = new NioEventLoopGroup();
        private final WebSocketVersion webSocketVersion;

        WebClient(int port) {
            this(port, WebSocketVersion.V13);
        }

        WebClient(int port, WebSocketVersion webSocketVersion) {
            this.uri = URI.create("ws://localhost:" + port);
            this.webSocketVersion = webSocketVersion;
        }

        public void open() throws Exception {
            Bootstrap b = new Bootstrap();
            String protocol = uri.getScheme();
            if (!"ws".equals(protocol)) {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }

            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, this.webSocketVersion, null, false, HttpHeaders.EMPTY_HEADERS, 1280000));

            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-codec", new HttpClientCodec());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                            pipeline.addLast("ws-handler", handler);
                        }
                    });

            //System.out.println("WebSocket Client connecting");
            clientChannel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();
        }

        public void write() {
            clientChannel.writeAndFlush(new TextWebSocketFrame("Hello World"));
        }

        public void close() throws InterruptedException {
            System.out.println("WebSocket Client sending close");
            if (this.clientChannel == null) {
                return;
            }

            clientChannel.writeAndFlush(new CloseWebSocketFrame());
            //clientChannel.closeFuture().sync();
            clientChannel.close();
            group.shutdownGracefully();
        }
    }

    private static class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
        private final WebSocketClientHandshaker handshaker;
        private ChannelPromise handshakeFuture;

        WebSocketClientHandler(final WebSocketClientHandshaker handshaker) {
            this.handshaker = handshaker;
        }

        public ChannelFuture handshakeFuture() {
            return handshakeFuture;
        }

        @Override
        public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception {
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
            System.out.println("WebSocket Client disconnected!");
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            final Channel ch = ctx.channel();
            if (!handshaker.isHandshakeComplete()) {
                // web socket client connected
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
                return;
            }

            if (msg instanceof FullHttpResponse) {
                final FullHttpResponse response = (FullHttpResponse) msg;
                throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content="
                        + response.content().toString(CharsetUtil.UTF_8) + ')');
            }

            final WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            } else if (frame instanceof CloseWebSocketFrame) {
                ch.close();
            }
        }

        @Override
        public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
            cause.printStackTrace();
            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }
    }
}
