/*
 * Copyright 2024 Google LLC
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
 */

package com.google.cloud.bigtable.examples.proxy.channelpool;

import static com.google.common.truth.Truth.assertThat;
import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.MutateRowRequest;
import com.google.bigtable.v2.MutateRowResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.type.Color;
import com.google.type.Money;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@RunWith(JUnit4.class)
public class ChannelPoolTest {
  private static final int DEFAULT_AWAIT_TERMINATION_SEC = 10;
  private ChannelPool pool;

  @After
  public void cleanup() throws InterruptedException {
    Preconditions.checkNotNull(pool, "Channel pool was never created");
    pool.shutdown();
    pool.awaitTermination(DEFAULT_AWAIT_TERMINATION_SEC, TimeUnit.SECONDS);
  }

  @Test
  public void testAuthority() throws IOException {
    ManagedChannel sub1 = Mockito.mock(ManagedChannel.class);
    ManagedChannel sub2 = Mockito.mock(ManagedChannel.class);

    Mockito.when(sub1.authority()).thenReturn("myAuth");

    pool =
        ChannelPool.create(
            ChannelPoolSettings.staticallySized(2),
            new FakeChannelFactory(Arrays.asList(sub1, sub2)));
    assertThat(pool.authority()).isEqualTo("myAuth");
  }

  @Test
  public void testRoundRobin() throws IOException {
    ManagedChannel sub1 = Mockito.mock(ManagedChannel.class);
    ManagedChannel sub2 = Mockito.mock(ManagedChannel.class);

    Mockito.when(sub1.authority()).thenReturn("myAuth");

    ArrayList<ManagedChannel> channels = Lists.newArrayList(sub1, sub2);
    pool =
        ChannelPool.create(
            ChannelPoolSettings.staticallySized(channels.size()), new FakeChannelFactory(channels));

    verifyTargetChannel(pool, channels, sub1);
    verifyTargetChannel(pool, channels, sub2);
    verifyTargetChannel(pool, channels, sub1);
  }

  private void verifyTargetChannel(
      ChannelPool pool, List<ManagedChannel> channels, ManagedChannel targetChannel) {
    MethodDescriptor<MutateRowRequest, MutateRowResponse> methodDescriptor =
        BigtableGrpc.getMutateRowMethod();
    CallOptions callOptions = CallOptions.DEFAULT;
    @SuppressWarnings("unchecked")
    ClientCall<MutateRowRequest, MutateRowResponse> expectedClientCall =
        Mockito.mock(ClientCall.class);

    channels.forEach(Mockito::reset);
    Mockito.doReturn(expectedClientCall).when(targetChannel).newCall(methodDescriptor, callOptions);

    ClientCall<MutateRowRequest, MutateRowResponse> actualCall =
        pool.newCall(methodDescriptor, callOptions);
    Mockito.verify(targetChannel, Mockito.times(1)).newCall(methodDescriptor, callOptions);
    actualCall.start(null, null);
    Mockito.verify(expectedClientCall, Mockito.times(1)).start(Mockito.any(), Mockito.any());

    for (ManagedChannel otherChannel : channels) {
      if (otherChannel != targetChannel) {
        Mockito.verify(otherChannel, Mockito.never()).newCall(methodDescriptor, callOptions);
      }
    }
  }

  @Test
  public void ensureEvenDistribution() throws InterruptedException, IOException {
    int numChannels = 10;
    final ManagedChannel[] channels = new ManagedChannel[numChannels];
    final AtomicInteger[] counts = new AtomicInteger[numChannels];

    MethodDescriptor<MutateRowRequest, MutateRowResponse> methodDescriptor =
        BigtableGrpc.getMutateRowMethod();
    final CallOptions callOptions = CallOptions.DEFAULT;
    @SuppressWarnings("unchecked")
    final ClientCall<MutateRowRequest, MutateRowResponse> clientCall =
        Mockito.mock(ClientCall.class);

    for (int i = 0; i < numChannels; i++) {
      final int index = i;

      counts[i] = new AtomicInteger();

      channels[i] = Mockito.mock(ManagedChannel.class);
      Mockito.when(channels[i].newCall(methodDescriptor, callOptions))
          .thenAnswer(
              (ignored) -> {
                counts[index].incrementAndGet();
                return clientCall;
              });
    }

    pool =
        ChannelPool.create(
            ChannelPoolSettings.staticallySized(numChannels),
            new FakeChannelFactory(Arrays.asList(channels)));

    int numThreads = 20;
    final int numPerThread = 1000;

    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    for (int i = 0; i < numThreads; i++) {
      executor.submit(
          () -> {
            for (int j = 0; j < numPerThread; j++) {
              pool.newCall(methodDescriptor, callOptions);
            }
          });
    }
    executor.shutdown();
    boolean shutdown = executor.awaitTermination(1, TimeUnit.MINUTES);
    assertThat(shutdown).isTrue();

    int expectedCount = (numThreads * numPerThread) / numChannels;
    for (AtomicInteger count : counts) {
      assertThat(count.get()).isAnyOf(expectedCount, expectedCount + 1);
    }
  }

  // Test channelPrimer is called same number of times as poolSize if executorService is set to null
  @Test
  public void channelPrimerShouldCallPoolConstruction() throws IOException {
    ChannelPrimer mockChannelPrimer = Mockito.mock(ChannelPrimer.class);
    ManagedChannel channel1 = Mockito.mock(ManagedChannel.class);
    ManagedChannel channel2 = Mockito.mock(ManagedChannel.class);

    pool =
        ChannelPool.create(
            ChannelPoolSettings.staticallySized(2).toBuilder()
                .setPreemptiveRefreshEnabled(true)
                .build(),
            new FakeChannelFactory(Arrays.asList(channel1, channel2), mockChannelPrimer));
    Mockito.verify(mockChannelPrimer, Mockito.times(2))
        .primeChannel(Mockito.any(ManagedChannel.class));
  }

  // Test channelPrimer is called periodically, if there's an executorService
  @Test
  public void channelPrimerIsCalledPeriodically() throws IOException {
    ChannelPrimer mockChannelPrimer = Mockito.mock(ChannelPrimer.class);
    ManagedChannel channel1 = Mockito.mock(ManagedChannel.class);
    ManagedChannel channel2 = Mockito.mock(ManagedChannel.class);
    ManagedChannel channel3 = Mockito.mock(ManagedChannel.class);

    List<Runnable> channelRefreshers = new ArrayList<>();

    ScheduledExecutorService scheduledExecutorService =
        Mockito.mock(ScheduledExecutorService.class);

    Answer<?> extractChannelRefresher =
        invocation -> {
          channelRefreshers.add(invocation.getArgument(0));
          return Mockito.mock(ScheduledFuture.class);
        };

    Mockito.doAnswer(extractChannelRefresher)
        .when(scheduledExecutorService)
        .scheduleAtFixedRate(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.any());

    FakeChannelFactory channelFactory =
        new FakeChannelFactory(Arrays.asList(channel1, channel2, channel3), mockChannelPrimer);

    pool =
        new ChannelPool(
            ChannelPoolSettings.staticallySized(1).toBuilder()
                .setPreemptiveRefreshEnabled(true)
                .build(),
            channelFactory,
            scheduledExecutorService);
    // 1 call during the creation
    Mockito.verify(mockChannelPrimer, Mockito.times(1))
        .primeChannel(Mockito.any(ManagedChannel.class));

    channelRefreshers.get(0).run();
    // 1 more call during channel refresh
    Mockito.verify(mockChannelPrimer, Mockito.times(2))
        .primeChannel(Mockito.any(ManagedChannel.class));

    channelRefreshers.get(0).run();
    // 1 more call during channel refresh
    Mockito.verify(mockChannelPrimer, Mockito.times(3))
        .primeChannel(Mockito.any(ManagedChannel.class));
  }

  // ----
  // call should be allowed to complete and the channel should not be shutdown
  @Test
  public void callShouldCompleteAfterCreation() throws IOException {
    ManagedChannel underlyingChannel = Mockito.mock(ManagedChannel.class);
    ManagedChannel replacementChannel = Mockito.mock(ManagedChannel.class);
    FakeChannelFactory channelFactory =
        new FakeChannelFactory(ImmutableList.of(underlyingChannel, replacementChannel));
    pool = ChannelPool.create(ChannelPoolSettings.staticallySized(1), channelFactory);

    // create a mock call when new call comes to the underlying channel
    MockClientCall<String, Integer> mockClientCall = new MockClientCall<>(1, Status.OK);
    MockClientCall<String, Integer> spyClientCall = Mockito.spy(mockClientCall);
    Mockito.when(
            underlyingChannel.newCall(
                Mockito.<MethodDescriptor<String, Integer>>any(), Mockito.any(CallOptions.class)))
        .thenReturn(spyClientCall);

    Answer<Object> verifyChannelNotShutdown =
        invocation -> {
          Mockito.verify(underlyingChannel, Mockito.never()).shutdown();
          return invocation.callRealMethod();
        };

    // verify that underlying channel is not shutdown when clientCall is still sending message
    Mockito.doAnswer(verifyChannelNotShutdown).when(spyClientCall).sendMessage(Mockito.anyString());

    // create a new call on entry
    @SuppressWarnings("unchecked")
    ClientCall.Listener<Integer> listener = Mockito.mock(ClientCall.Listener.class);
    ClientCall<String, Integer> call =
        pool.newCall(FakeMethodDescriptor.create(), CallOptions.DEFAULT);

    pool.refresh();
    // shutdown is not called because there is still an outstanding call, even if it hasn't started
    Mockito.verify(underlyingChannel, Mockito.after(200).never()).shutdown();

    // start clientCall
    call.start(listener, new Metadata());
    // send message and end the call
    call.sendMessage("message");
    // shutdown is called because the outstanding call has completed
    Mockito.verify(underlyingChannel, Mockito.atLeastOnce()).shutdown();

    // Replacement channel shouldn't be touched
    Mockito.verify(replacementChannel, Mockito.never()).shutdown();
    Mockito.verify(replacementChannel, Mockito.never()).newCall(Mockito.any(), Mockito.any());
  }

  // call should be allowed to complete and the channel should not be shutdown
  @Test
  public void callShouldCompleteAfterStarted() throws IOException {
    final ManagedChannel underlyingChannel = Mockito.mock(ManagedChannel.class);
    ManagedChannel replacementChannel = Mockito.mock(ManagedChannel.class);

    FakeChannelFactory channelFactory =
        new FakeChannelFactory(ImmutableList.of(underlyingChannel, replacementChannel));
    pool = ChannelPool.create(ChannelPoolSettings.staticallySized(1), channelFactory);

    // create a mock call when new call comes to the underlying channel
    MockClientCall<String, Integer> mockClientCall = new MockClientCall<>(1, Status.OK);
    MockClientCall<String, Integer> spyClientCall = Mockito.spy(mockClientCall);
    Mockito.when(
            underlyingChannel.newCall(
                Mockito.<MethodDescriptor<String, Integer>>any(), Mockito.any(CallOptions.class)))
        .thenReturn(spyClientCall);

    Answer<Object> verifyChannelNotShutdown =
        invocation -> {
          Mockito.verify(underlyingChannel, Mockito.never()).shutdown();
          return invocation.callRealMethod();
        };

    // verify that underlying channel is not shutdown when clientCall is still sending message
    Mockito.doAnswer(verifyChannelNotShutdown).when(spyClientCall).sendMessage(Mockito.anyString());

    // create a new call on safeShutdownManagedChannel
    @SuppressWarnings("unchecked")
    ClientCall.Listener<Integer> listener = Mockito.mock(ClientCall.Listener.class);
    ClientCall<String, Integer> call =
        pool.newCall(FakeMethodDescriptor.create(), CallOptions.DEFAULT);

    // start clientCall
    call.start(listener, new Metadata());
    pool.refresh();

    // shutdown is not called because there is still an outstanding call
    Mockito.verify(underlyingChannel, Mockito.after(200).never()).shutdown();
    // send message and end the call
    call.sendMessage("message");
    // shutdown is called because the outstanding call has completed
    Mockito.verify(underlyingChannel, Mockito.atLeastOnce()).shutdown();
  }

  // Channel should be shutdown after a refresh all the calls have completed
  @Test
  public void channelShouldShutdown() throws IOException {
    ManagedChannel underlyingChannel = Mockito.mock(ManagedChannel.class);
    ManagedChannel replacementChannel = Mockito.mock(ManagedChannel.class);

    FakeChannelFactory channelFactory =
        new FakeChannelFactory(ImmutableList.of(underlyingChannel, replacementChannel));
    pool = ChannelPool.create(ChannelPoolSettings.staticallySized(1), channelFactory);

    // create a mock call when new call comes to the underlying channel
    MockClientCall<String, Integer> mockClientCall = new MockClientCall<>(1, Status.OK);
    MockClientCall<String, Integer> spyClientCall = Mockito.spy(mockClientCall);
    Mockito.when(
            underlyingChannel.newCall(
                Mockito.<MethodDescriptor<String, Integer>>any(), Mockito.any(CallOptions.class)))
        .thenReturn(spyClientCall);

    Answer<Object> verifyChannelNotShutdown =
        invocation -> {
          Mockito.verify(underlyingChannel, Mockito.never()).shutdown();
          return invocation.callRealMethod();
        };

    // verify that underlying channel is not shutdown when clientCall is still sending message
    Mockito.doAnswer(verifyChannelNotShutdown).when(spyClientCall).sendMessage(Mockito.anyString());

    // create a new call on safeShutdownManagedChannel
    @SuppressWarnings("unchecked")
    ClientCall.Listener<Integer> listener = Mockito.mock(ClientCall.Listener.class);
    ClientCall<String, Integer> call =
        pool.newCall(FakeMethodDescriptor.create(), CallOptions.DEFAULT);

    // start clientCall
    call.start(listener, new Metadata());
    // send message and end the call
    call.sendMessage("message");
    // shutdown is not called because it has not been shutdown yet
    Mockito.verify(underlyingChannel, Mockito.after(200).never()).shutdown();
    pool.refresh();
    // shutdown is called because the outstanding call has completed
    Mockito.verify(underlyingChannel, Mockito.atLeastOnce()).shutdown();
  }

  @Test
  public void channelRefreshShouldSwapChannels() throws IOException {
    ManagedChannel underlyingChannel1 = Mockito.mock(ManagedChannel.class);
    ManagedChannel underlyingChannel2 = Mockito.mock(ManagedChannel.class);

    // mock executor service to capture the runnable scheduled, so we can invoke it when we want to
    ScheduledExecutorService scheduledExecutorService =
        Mockito.mock(ScheduledExecutorService.class);

    Mockito.doReturn(null)
        .when(scheduledExecutorService)
        .schedule(
            Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.MILLISECONDS));

    FakeChannelFactory channelFactory =
        new FakeChannelFactory(ImmutableList.of(underlyingChannel1, underlyingChannel2));
    pool =
        new ChannelPool(
            ChannelPoolSettings.staticallySized(1).toBuilder()
                .setPreemptiveRefreshEnabled(true)
                .build(),
            channelFactory,
            scheduledExecutorService);
    Mockito.reset(underlyingChannel1);

    pool.newCall(FakeMethodDescriptor.<String, Integer>create(), CallOptions.DEFAULT);

    Mockito.verify(underlyingChannel1, Mockito.only())
        .newCall(Mockito.<MethodDescriptor<String, Integer>>any(), Mockito.any(CallOptions.class));

    // swap channel
    pool.refresh();

    pool.newCall(FakeMethodDescriptor.<String, Integer>create(), CallOptions.DEFAULT);

    Mockito.verify(underlyingChannel2, Mockito.only())
        .newCall(Mockito.<MethodDescriptor<String, Integer>>any(), Mockito.any(CallOptions.class));
  }

  @Test
  public void channelCountShouldNotChangeWhenOutstandingRpcsAreWithinLimits() throws Exception {
    ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);

    List<ClientCall<Object, Object>> startedCalls = new ArrayList<>();

    ChannelFactory channelFactory =
        () -> {
          ManagedChannel channel = Mockito.mock(ManagedChannel.class);
          Mockito.when(channel.newCall(Mockito.any(), Mockito.any()))
              .thenAnswer(
                  invocation -> {
                    @SuppressWarnings("unchecked")
                    ClientCall<Object, Object> clientCall = Mockito.mock(ClientCall.class);
                    startedCalls.add(clientCall);
                    return clientCall;
                  });
          return channel;
        };

    pool =
        new ChannelPool(
            ChannelPoolSettings.builder()
                .setInitialChannelCount(2)
                .setMinRpcsPerChannel(1)
                .setMaxRpcsPerChannel(2)
                .build(),
            channelFactory,
            executor);
    assertThat(pool.entries.get()).hasSize(2);

    // Start the minimum number of
    for (int i = 0; i < 2; i++) {
      ClientCalls.futureUnaryCall(
          pool.newCall(BigtableGrpc.getMutateRowMethod(), CallOptions.DEFAULT),
          MutateRowRequest.getDefaultInstance());
    }
    pool.resize();
    assertThat(pool.entries.get()).hasSize(2);

    // Add enough RPCs to be just at the brink of expansion
    for (int i = startedCalls.size(); i < 4; i++) {
      ClientCalls.futureUnaryCall(
          pool.newCall(BigtableGrpc.getMutateRowMethod(), CallOptions.DEFAULT),
          MutateRowRequest.getDefaultInstance());
    }
    pool.resize();
    assertThat(pool.entries.get()).hasSize(2);

    // Add another RPC to push expansion
    pool.newCall(BigtableGrpc.getMutateRowMethod(), CallOptions.DEFAULT);
    pool.resize();
    assertThat(pool.entries.get()).hasSize(4); // += ChannelPool::MAX_RESIZE_DELTA
    assertThat(startedCalls).hasSize(5);

    // Complete RPCs to the brink of shrinking
    @SuppressWarnings("unchecked")
    ArgumentCaptor<ClientCall.Listener<Object>> captor =
        ArgumentCaptor.forClass(ClientCall.Listener.class);
    Mockito.verify(startedCalls.remove(0)).start(captor.capture(), Mockito.any());
    captor.getValue().onClose(Status.ABORTED, new Metadata());
    // Resize twice: the first round maintains the peak from the last cycle
    pool.resize();
    pool.resize();
    assertThat(pool.entries.get()).hasSize(4);
    assertThat(startedCalls).hasSize(4);

    // Complete another RPC to trigger shrinking
    Mockito.verify(startedCalls.remove(0)).start(captor.capture(), Mockito.any());
    captor.getValue().onClose(Status.ABORTED, new Metadata());
    // Resize twice: the first round maintains the peak from the last cycle
    pool.resize();
    pool.resize();
    assertThat(startedCalls).hasSize(3);
    // range of channels is [2-3] rounded down average is 2
    assertThat(pool.entries.get()).hasSize(2);
  }

  @Test
  public void removedIdleChannelsAreShutdown() throws Exception {
    ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);

    List<ManagedChannel> channels = new ArrayList<>();

    ChannelFactory channelFactory =
        () -> {
          ManagedChannel channel = Mockito.mock(ManagedChannel.class);
          Mockito.when(channel.newCall(Mockito.any(), Mockito.any()))
              .thenAnswer(
                  invocation -> {
                    @SuppressWarnings("unchecked")
                    ClientCall<Object, Object> clientCall = Mockito.mock(ClientCall.class);
                    return clientCall;
                  });

          channels.add(channel);
          return channel;
        };

    pool =
        new ChannelPool(
            ChannelPoolSettings.builder()
                .setInitialChannelCount(2)
                .setMinRpcsPerChannel(1)
                .setMaxRpcsPerChannel(2)
                .build(),
            channelFactory,
            executor);
    assertThat(pool.entries.get()).hasSize(2);

    // With no outstanding RPCs, the pool should shrink
    pool.resize();
    assertThat(pool.entries.get()).hasSize(1);
    Mockito.verify(channels.get(1), Mockito.times(1)).shutdown();
  }

  @Test
  public void removedActiveChannelsAreShutdown() throws Exception {
    ScheduledExecutorService executor = Mockito.mock(ScheduledExecutorService.class);

    List<ManagedChannel> channels = new ArrayList<>();
    List<ClientCall<Object, Object>> startedCalls = new ArrayList<>();

    ChannelFactory channelFactory =
        () -> {
          ManagedChannel channel = Mockito.mock(ManagedChannel.class);
          Mockito.when(channel.newCall(Mockito.any(), Mockito.any()))
              .thenAnswer(
                  invocation -> {
                    @SuppressWarnings("unchecked")
                    ClientCall<Object, Object> clientCall = Mockito.mock(ClientCall.class);
                    startedCalls.add(clientCall);
                    return clientCall;
                  });

          channels.add(channel);
          return channel;
        };

    pool =
        new ChannelPool(
            ChannelPoolSettings.builder()
                .setInitialChannelCount(2)
                .setMinRpcsPerChannel(1)
                .setMaxRpcsPerChannel(2)
                .build(),
            channelFactory,
            executor);
    assertThat(pool.entries.get()).hasSize(2);

    // Start 2 RPCs
    for (int i = 0; i < 2; i++) {
      ClientCalls.futureUnaryCall(
          pool.newCall(BigtableGrpc.getMutateRowMethod(), CallOptions.DEFAULT),
          MutateRowRequest.getDefaultInstance());
    }
    // Complete the first one
    @SuppressWarnings("unchecked")
    ArgumentCaptor<ClientCall.Listener<Object>> captor =
        ArgumentCaptor.forClass(ClientCall.Listener.class);
    Mockito.verify(startedCalls.get(0)).start(captor.capture(), Mockito.any());
    captor.getValue().onClose(Status.ABORTED, new Metadata());

    // With a single RPC, the pool should shrink
    pool.resize();
    pool.resize();
    assertThat(pool.entries.get()).hasSize(1);

    // While the RPC is outstanding, the channel should still be open
    Mockito.verify(channels.get(1), Mockito.never()).shutdown();

    // Complete the RPC
    Mockito.verify(startedCalls.get(1)).start(captor.capture(), Mockito.any());
    captor.getValue().onClose(Status.ABORTED, new Metadata());
    // Now the channel should be closed
    Mockito.verify(channels.get(1), Mockito.times(1)).shutdown();
  }

  @Test
  public void testReleasingClientCallCancelEarly() throws IOException {
    @SuppressWarnings("unchecked")
    ClientCall<Object, Object> mockClientCall = Mockito.mock(ClientCall.class);
    Mockito.doAnswer(invocation -> null).when(mockClientCall).cancel(Mockito.any(), Mockito.any());
    ManagedChannel fakeChannel = Mockito.mock(ManagedChannel.class);
    Mockito.when(fakeChannel.newCall(Mockito.any(), Mockito.any())).thenReturn(mockClientCall);
    ChannelPoolSettings channelPoolSettings = ChannelPoolSettings.staticallySized(1);
    ChannelFactory factory = new FakeChannelFactory(ImmutableList.of(fakeChannel));
    pool = ChannelPool.create(channelPoolSettings, factory);

    ClientCall<MutateRowRequest, MutateRowResponse> call =
        pool.newCall(BigtableGrpc.getMutateRowMethod(), CallOptions.DEFAULT);
    call.cancel(null, null);

    IllegalStateException e =
        Assert.assertThrows(
            IllegalStateException.class, () -> call.start(new Listener<>() {}, new Metadata()));
    assertThat(e.getCause()).isInstanceOf(CancellationException.class);
    assertThat(e.getMessage()).isEqualTo("Call is already cancelled");
  }

  @Test
  public void testDoubleRelease() throws Exception {
    FakeLogHandler logHandler = new FakeLogHandler();
    ChannelPool.LOG.addHandler(logHandler);

    try {
      // Create a fake channel pool thats backed by mock channels that simply record invocations
      @SuppressWarnings("unchecked")
      ClientCall<MutateRowRequest, MutateRowResponse> mockClientCall =
          Mockito.mock(ClientCall.class);
      ManagedChannel fakeChannel = Mockito.mock(ManagedChannel.class);
      Mockito.when(
              fakeChannel.newCall(
                  Mockito.eq(BigtableGrpc.getMutateRowMethod()), Mockito.any(CallOptions.class)))
          .thenReturn(mockClientCall);
      ChannelPoolSettings channelPoolSettings = ChannelPoolSettings.staticallySized(1);
      ChannelFactory factory = new FakeChannelFactory(ImmutableList.of(fakeChannel));

      pool = ChannelPool.create(channelPoolSettings, factory);

      // Start the RPC
      ListenableFuture<MutateRowResponse> rpcFuture =
          BigtableGrpc.newFutureStub(pool).mutateRow(MutateRowRequest.getDefaultInstance());

      // Get the server side listener and intentionally close it twice
      @SuppressWarnings("unchecked")
      ArgumentCaptor<ClientCall.Listener<MutateRowResponse>> clientCallListenerCaptor =
          ArgumentCaptor.forClass(ClientCall.Listener.class);

      Mockito.verify(mockClientCall).start(clientCallListenerCaptor.capture(), Mockito.any());
      clientCallListenerCaptor.getValue().onClose(Status.INTERNAL, new Metadata());
      clientCallListenerCaptor.getValue().onClose(Status.UNKNOWN, new Metadata());

      // Ensure that the channel pool properly logged the double call and kept the refCount correct
      assertThat(logHandler.getAllMessages())
          .contains(
              "Call is being closed more than once. Please make sure that onClose() is not being"
                  + " manually called.");
      assertThat(pool.entries.get()).hasSize(1);
      ChannelPool.Entry entry = pool.entries.get().get(0);
      assertThat(entry.outstandingRpcs.get()).isEqualTo(0);
    } finally {
      ChannelPool.LOG.removeHandler(logHandler);
    }
  }

  static class FakeChannelFactory implements ChannelFactory {
    private int called = 0;
    private final List<ManagedChannel> channels;
    private ChannelPrimer channelPrimer;

    public FakeChannelFactory(List<ManagedChannel> channels) {
      this.channels = channels;
    }

    public FakeChannelFactory(List<ManagedChannel> channels, ChannelPrimer channelPrimer) {
      this.channels = channels;
      this.channelPrimer = channelPrimer;
    }

    public ManagedChannel createSingleChannel() {
      ManagedChannel managedChannel = channels.get(called++);
      if (this.channelPrimer != null) {
        this.channelPrimer.primeChannel(managedChannel);
      }
      return managedChannel;
    }
  }

  static class FakeLogHandler extends Handler {
    List<LogRecord> records = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
      records.add(record);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    public List<String> getAllMessages() {
      return records.stream().map(LogRecord::getMessage).collect(Collectors.toList());
    }
  }

  public interface ChannelPrimer {
    void primeChannel(ManagedChannel managedChannel);
  }

  static class MockClientCall<RequestT, ResponseT> extends ClientCall<RequestT, ResponseT> {

    private final ResponseT response;
    private Listener<ResponseT> responseListener;
    private Metadata headers;
    private final Status status;

    public MockClientCall(ResponseT response, Status status) {
      this.response = response;
      this.status = status;
    }

    @Override
    public synchronized void start(Listener<ResponseT> responseListener, Metadata headers) {
      this.responseListener = responseListener;
      this.headers = headers;
    }

    @Override
    public void request(int numMessages) {}

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {}

    @Override
    public void halfClose() {}

    @Override
    public void sendMessage(RequestT message) {
      responseListener.onHeaders(headers);
      responseListener.onMessage(response);
      responseListener.onClose(status, headers);
    }
  }

  static class FakeMethodDescriptor {
    // Utility class, uninstantiable.
    private FakeMethodDescriptor() {}

    public static <I, O> MethodDescriptor<I, O> create() {
      return create(MethodDescriptor.MethodType.UNARY, "FakeClient/fake-method");
    }

    public static <I, O> MethodDescriptor<I, O> create(
        MethodDescriptor.MethodType type, String name) {
      return MethodDescriptor.<I, O>newBuilder()
          .setType(MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(name)
          .setRequestMarshaller(new FakeMarshaller<I>())
          .setResponseMarshaller(new FakeMarshaller<O>())
          .build();
    }

    private static class FakeMarshaller<T> implements MethodDescriptor.Marshaller<T> {
      @Override
      public T parse(InputStream stream) {
        throw new UnsupportedOperationException("FakeMarshaller doesn't actually do anything");
      }

      @Override
      public InputStream stream(T value) {
        throw new UnsupportedOperationException("FakeMarshaller doesn't actually do anything");
      }
    }
  }

  static final MethodDescriptor<Color, Money> METHOD_RECOGNIZE =
      MethodDescriptor.<Color, Money>newBuilder()
          .setType(MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName("google.gax.FakeService", "Recognize"))
          .setRequestMarshaller(ProtoUtils.marshaller(Color.getDefaultInstance()))
          .setResponseMarshaller(ProtoUtils.marshaller(Money.getDefaultInstance()))
          .build();

  public static final MethodDescriptor<Color, Money> METHOD_SERVER_STREAMING_RECOGNIZE =
      MethodDescriptor.<Color, Money>newBuilder()
          .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
          .setFullMethodName(
              generateFullMethodName("google.gax.FakeService", "ServerStreamingRecognize"))
          .setRequestMarshaller(ProtoUtils.marshaller(Color.getDefaultInstance()))
          .setResponseMarshaller(ProtoUtils.marshaller(Money.getDefaultInstance()))
          .build();
}
