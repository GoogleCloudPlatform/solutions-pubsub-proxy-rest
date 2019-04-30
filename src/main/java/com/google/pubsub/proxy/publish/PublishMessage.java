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

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.proxy.entities.Message;
import com.google.pubsub.proxy.entities.Request;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubMessage.Builder;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/** Publishes messages to Cloud Pub/Sub */
@Path("/publish")
public class PublishMessage {

  private static final String projectId = ServiceOptions.getDefaultProjectId();
  private Map<String, Publisher> publishers = new ConcurrentHashMap<>();
  private static final Logger LOGGER = Logger.getLogger(PublishMessage.class.getName());

  private Map<String, Publisher> getPublishers() {
    return publishers;
  }

  void setPublishers(Map<String, Publisher> publishers) {
    this.publishers = Collections.unmodifiableMap(publishers);
  }

  /**
   * Entry point for POST /publish Enforces token validation
   *
   * @param req - POJO translated user request
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response doPost(Request req) throws Exception {

    if (null == req.getTopic()) {
      return invalidRequest("Pub/Sub topic required");
    }
    if (null == req.getMessages()) {
      return invalidRequest("Message required");
    }
    if (req.getMessages().isEmpty()) {
      return invalidRequest("Message cannot be empty");
    }

    Publisher publisher = getPublisher(req.getTopic());
    for (final Message msg : req.getMessages()) {
      publishMessage(publisher, msg);
    }

    return Response.ok().build();
  }

  /** Populates PubSub publisher Publishes messages downstream */
  private void publishMessage(Publisher publisher, Message msg) throws Exception {

    Builder builder = PubsubMessage.newBuilder();
    if (null != msg.getData()) {
      builder.setData(ByteString.copyFromUtf8(msg.getData()));
    }
    if (null != msg.getMessageId()) {
      builder.setMessageId(msg.getMessageId());
    }
    if (null != msg.getPublishTime()) {
      builder.setPublishTime(getTimeStamp(msg.getPublishTime()));
    }
    if (null != msg.getAttributes()) {
      builder.putAllAttributes(getAllAttributes(msg.getAttributes()));
    }

    ApiFuture<String> future = publisher.publish(builder.build());
    ApiFutures.addCallback(
        future,
        new ApiFutureCallback<String>() {
          public void onFailure(Throwable throwable) {
            LOGGER.severe("Failed to publish message: " + throwable.getMessage());
          }

          public void onSuccess(String msgId) {
            LOGGER.info("Successfully published: " + msgId);
          }
        },
        MoreExecutors.directExecutor());
  }

  /** Creates PubSub publisher if one doesn't exist */
  private Publisher getPublisher(String topic) throws IOException {

    if (getPublishers().containsKey(topic)) {
      return getPublishers().get(topic);
    }

    LOGGER.info("Creating new publisher for: " + topic);
    Publisher publisher = Publisher.newBuilder(ProjectTopicName.of(projectId, topic)).build();
    getPublishers().put(topic, publisher);
    return publisher;
  }

  /** Handle missing parameters in incoming requests */
  private Response invalidRequest(String msg) {
    return Response.status(Status.BAD_REQUEST).entity(msg).type(MediaType.APPLICATION_JSON).build();
  }

  /** Returns timestamp from string */
  private Timestamp getTimeStamp(String s) throws ParseException {
    return Timestamps.parse(s);
  }

  /** Attributes captured as generic object returned in the format understandable by PubSub */
  @SuppressWarnings("unchecked")
  private static Map<String, String> getAllAttributes(Object attributes) {
    return (Map<String, String>) attributes;
  }
}
