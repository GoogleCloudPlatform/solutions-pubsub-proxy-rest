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

import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

public class HealthCheckTest extends JerseyTest {

  @Override
  protected DeploymentContext configureDeployment() {
    return ServletDeploymentContext.forServlet(
            new ServletContainer(new ResourceConfig(HealthCheck.class)))
        .build();
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
    return new GrizzlyWebTestContainerFactory();
  }

  @Test
  public void HealthCheckAlwaysReturnsStatusOK() {
    Response response = target("/").request().get();
    Assert.assertEquals(OK.getStatusCode(), response.getStatus());
  }
}
