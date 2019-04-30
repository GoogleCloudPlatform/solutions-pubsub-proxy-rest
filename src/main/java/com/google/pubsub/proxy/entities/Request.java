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

package com.google.pubsub.proxy.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a request to publish one or more messages to Cloud Pub/Sub.
 *
 * Example user request json:
 * { "topic": "test",
 *   "messages": [ { "attributes": { "key1":"value1", "key2" : "value2" ... },
 *   "data": "sample data",
 *   "messageId": "123",
 * "publishTime": "...timestamp..." } ] }
 */
public class Request {

  @JsonProperty("topic")
  private String topic;

  @JsonProperty("messages")
  private List<Message> messages;

  @JsonProperty("topic")
  public String getTopic() {
    return topic;
  }

  @JsonProperty("topic")
  public void setTopic(String topic) {
    this.topic = topic;
  }

  @JsonProperty("messages")
  public List<Message> getMessages() {
    return messages;
  }

  @JsonProperty("messages")
  public void setMessages(List<Message> messages) {
    this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
  }
}
