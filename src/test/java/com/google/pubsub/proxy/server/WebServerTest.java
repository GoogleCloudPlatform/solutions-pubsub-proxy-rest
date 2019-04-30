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

package com.google.pubsub.proxy.server;

import static com.google.pubsub.proxy.server.WebServer.main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WebServerTest {

  protected static class ThreadRunner implements Runnable {

    @Override
    public void run() {
      try {
        main(new String[] {});
      } catch (InterruptedException e) {
        System.out.println("Server terminated successfully");
      } catch (Exception e) {
        System.out.println("Unable to start the application");
        e.printStackTrace();
      }
    }
  }

  private ExecutorService executorService;

  @Before
  public void setUp() {
    executorService = Executors.newSingleThreadExecutor();
    executorService.submit(new ThreadRunner());
  }

  @After
  public void tearDown() {
    executorService.shutdownNow();
  }

  @Test(timeout = 60000)
  public void whenTheApplicationStartsJettyServerIsStarted() throws InterruptedException {
    HttpUriRequest request = new HttpGet("http://localhost:8080");
    boolean success = false;
    while (!success) {
      try {
        HttpResponse response = HttpClientBuilder.create().build().execute(request);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        success = true;
      } catch (Exception e) {
        success = false;
        System.out.println("Server not started yet. Waiting for another second before retrying.");
        Thread.sleep(1000);
      }
    }
  }
}
