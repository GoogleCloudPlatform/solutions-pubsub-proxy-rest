/* Copyright 2019 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package com.google.pubsub.proxy.publish;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.proxy.entities.Message;
import com.google.pubsub.proxy.entities.Request;
import com.google.pubsub.v1.PubsubMessage;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishMessageTest {

  private static final String TOPIC = "PUBSUB_TOPIC";
  private static final String MESSAGE_ID = "MESSAGE_ID";
  private static final String DATA = "MESSAGE_DATA";
  private static final HashMap<String, String> ATTRIBUTES = new HashMap<>();
  private static final String PUBLISH_TIME =
      ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  @Mock Map<String, Publisher> publisherList;
  @Mock Publisher publisher;
  @Captor ArgumentCaptor<PubsubMessage> captor;
  private PublishMessage publishMessage;
  private Request request;
  private Message message;
  private ApiFuture<String> goodFuture;
  private ApiFuture<String> badFuture;

  @Before
  public void setUp() {
    publishMessage = new PublishMessage();
    setupRequest();
    setupMockPublisher();
    setupFutures();
  }

  private void setupRequest() {
    request = new Request();
    request.setTopic(TOPIC);
    setupMessage();
    request.setMessages(Collections.singletonList(message));
  }

  private void setupMessage() {
    message = new Message();
    message.setMessageId(MESSAGE_ID);
    message.setData(DATA);
    message.setPublishTime(PUBLISH_TIME);
    message.setAttributes(ATTRIBUTES);
    publishMessage.setPublishers(publisherList);
  }

  private void setupFutures() {
    goodFuture = getSuccessfulPublishFuture();
    badFuture = getFailedPublishFuture();
  }

  @After
  public void tearDown() {}

  @Test
  public void WhenRequestIsValidAndTopicExistsThenRespectivePublisherIsReturned() throws Exception {
    when(publisher.publish(any())).thenReturn(goodFuture);
    publishMessage.doPost(request);
    verify(publisher).publish(Mockito.any());
  }

  @Test
  public void WhenRequestIsValidAndSingleMessageExistsThenPublishIsInvokedOnce() throws Exception {
    when(publisher.publish(any())).thenReturn(goodFuture);
    publishMessage.doPost(request);
    verify(publisher, times(1)).publish(Mockito.any());
  }

  @Test
  public void WhenRequestIsValidAndTwoMessagesExistsThenPublishIsInvokedTwice() throws Exception {
    ArrayList<Message> messages = new ArrayList<>();
    messages.add(message);
    messages.add(message);
    request.setMessages(messages);
    when(publisher.publish(any())).thenReturn(goodFuture);
    publishMessage.doPost(request);
    verify(publisher, times(2)).publish(Mockito.any());
  }

  @Test
  public void WhenRequestIsValidAndPublisherInitializedThenPubSubMessagesArePublished()
      throws Exception {
    when(publisher.publish(any())).thenReturn(goodFuture);
    publishMessage.doPost(request);
    verify(publisher).publish(captor.capture());
    assertEquals(DATA, captor.getAllValues().get(0).getData().toStringUtf8());
  }

  @Test
  public void WhenPublishIsSuccessfulThenOnSuccessCallbackIsInvokedOntheFuture() throws Exception {
    when(publisher.publish(any())).thenReturn(goodFuture);
    publishMessage.doPost(request);
    verify(goodFuture, times(1)).addListener(any(Runnable.class), any(Executor.class));
  }

  @Test
  public void WhenPublishFailsThenOnFailureCallbackIsInvokedOntheFuture() throws Exception {
    when(publisher.publish(any())).thenReturn(badFuture);
    publishMessage.doPost(request);
    verify(badFuture, times(1)).addListener(any(Runnable.class), any(Executor.class));
  }

  @Test(expected = Exception.class)
  public void WhenMessageDataIsNullThenExceptionIsThrown() throws Exception {
    message.setData(null);
    publishMessage.doPost(request);
  }

  @Test(expected = Exception.class)
  public void WhenMessageIdIsNullThenExceptionIsThrown() throws Exception {
    message.setMessageId(null);
    publishMessage.doPost(request);
  }

  @Test(expected = Exception.class)
  public void WhenMessageAttributesAreNullThenExceptionIsThrown() throws Exception {
    message.setAttributes(null);
    publishMessage.doPost(request);
  }

  @Test(expected = Exception.class)
  public void WhenMessagePublishTimeIsNullThenExceptionIsThrown() throws Exception {
    message.setPublishTime(null);
    publishMessage.doPost(request);
  }

  private void setupMockPublisher() {
    when(publisherList.containsKey(TOPIC)).thenReturn(Boolean.TRUE);
    when(publisherList.get(TOPIC)).thenReturn(publisher);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ApiFuture<String> getSuccessfulPublishFuture() {
    SpyableFuture<String> future = new SpyableFuture("success");
    return spy(future);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ApiFuture<String> getFailedPublishFuture() {
    SpyableFuture<String> future = new SpyableFuture(new Exception());
    return spy(future);
  }

  private class SpyableFuture<V> implements ApiFuture<V> {
    private V value = null;
    private Exception exception = null;

    public SpyableFuture(V value) {
      this.value = value;
    }

    public SpyableFuture(Exception exception) {
      this.exception = exception;
    }

    @Override
    public V get() throws ExecutionException {
      if (exception != null) {
        throw new ExecutionException(exception);
      }
      return value;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws ExecutionException {
      return get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
      executor.execute(listener);
    }
  }
}
