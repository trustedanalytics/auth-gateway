/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.trustedanalytics.auth.gateway.engine.async;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.trustedanalytics.auth.gateway.engine.RequestApplier;
import org.trustedanalytics.auth.gateway.spi.AuthorizableGatewayException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncComponent.class);

  private final Cache<UUID, AsyncState> states;

  private final int timeout;

  public AsyncComponent(int timeout) {
    states = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(1, TimeUnit.HOURS).build();
    this.timeout = timeout;
  }

  public AsyncState addTask(RequestApplier supplier) throws AuthorizableGatewayException {
    UUID uuid = getUUID();
    CompletableFuture<AsyncState> completableFuture = CompletableFuture.supplyAsync(() -> requestParser(supplier, uuid));

    try {
      return completableFuture.get(timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      return getRequestState(uuid);
    } catch (Exception e) {
      throw new AuthorizableGatewayException("Async task failed", e);
    }
  }

  public AsyncState getRequestState(UUID uuid) throws AuthorizableGatewayException {
    AsyncState state = executeCheckedQuery(() -> {
      if (states.asMap().containsKey(uuid))
        return states.asMap().get(uuid);
      return AsyncState.INVALID_STATE;
    });

    if (state == AsyncState.INVALID_STATE)
      throw new NoContentException();

    if (state.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR)
      throw (AuthorizableGatewayException) state.getResult();

    if (!state.isDone())
      throw new TaskNotFinishedException(new AsyncRequestState(state.getSubmited(), getRequestUrl(uuid)));

    return state;
  }

  private String getRequestUrl(UUID uuid) {
    return String.format("/jobs/%s", uuid.toString());
  }

  private UUID getUUID() throws AuthorizableGatewayException {
    Retryer<UUID> guidRetryer = RetryerBuilder.<UUID>newBuilder().retryIfResult((e) -> states.asMap().containsKey(e))
            .withStopStrategy(StopStrategies.stopAfterAttempt(5)).build();
    UUID uuid = executeCheckedQuery(() -> {
      try{
      return guidRetryer.call(() -> UUID.randomUUID());}
      catch (RetryException e)
      {
        throw new AuthorizableGatewayException("Unable to obtain new UUID", e);
      }
      catch (ExecutionException e)
      {
        throw new AuthorizableGatewayException("Obtaining new job UUID faled", e);
      }
    });
    states.put(uuid, new AsyncState());
    return uuid;
  }

  private synchronized <T> T executeCheckedQuery(AsyncOperation<T, AuthorizableGatewayException> query)
          throws AuthorizableGatewayException {
    return query.get();
  }

  private void executeQuery(Runnable query)
  {
    try{
      executeCheckedQuery(() -> {
        query.run();
        return null;
      });
    }
    catch (AuthorizableGatewayException e)
    {
      LOGGER.error("Async query failed", e);
    }
  }

  private AsyncState requestParser(RequestApplier supplier, UUID uuid) {
    AsyncState state = states.asMap().get(uuid);
    try {
      Object result = supplier.parseRequest();
      HttpStatus status = HttpStatus.OK;
      state.setState(status, result);
    } catch (Exception e) {
      LOGGER.error("Request failed", e);
      Object result = new AuthorizableGatewayException("Failed", e);
      HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
      state.setState(status, result);
    }
    return state;
  }

}
