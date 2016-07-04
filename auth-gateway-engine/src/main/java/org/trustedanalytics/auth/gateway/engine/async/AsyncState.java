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

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.trustedanalytics.auth.gateway.engine.AuthgatewayResponse;

import java.util.Date;

@Data
public class AsyncState implements AuthgatewayResponse {

  public static final AsyncState INVALID_STATE = new AsyncState();

  private boolean done;

  private HttpStatus status;

  private Object result;

  private Date submited;

  public AsyncState() {
    done = false;

    status = HttpStatus.I_AM_A_TEAPOT;

    result = null;

    submited = new Date();
  }

  public void setState(HttpStatus status, Object result) {
    this.done = true;
    this.status = status;
    this.result = result;
  }
}
