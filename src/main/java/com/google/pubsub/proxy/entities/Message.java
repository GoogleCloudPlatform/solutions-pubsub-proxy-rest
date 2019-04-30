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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** Represents a message to be published to a Cloud Pub/Sub topic */
@JsonPropertyOrder({"attributes", "data", "messageId", "publishTime"})
public class Message {

  @JsonProperty("attributes")
  private Object attributes;

  @JsonProperty("data")
  private String data;

  @JsonProperty("messageId")
  private String messageId;

  @JsonProperty("publishTime")
  private String publishTime;

  @JsonProperty("attributes")
  public Object getAttributes() {
    return attributes;
  }

  @JsonProperty("attributes")
  public void setAttributes(Object attributes) {
    this.attributes = attributes;
  }

  @JsonProperty("data")
  public String getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(String data) {
    this.data = data;
  }

  @JsonProperty("messageId")
  public String getMessageId() {
    return messageId;
  }

  @JsonProperty("messageId")
  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  @JsonProperty("publishTime")
  public String getPublishTime() {
    return publishTime;
  }

  @JsonProperty("publishTime")
  public void setPublishTime(String publishTime) {
    this.publishTime = publishTime;
  }
}
