/*
 * Copyright 2017 Daniel Wamara (dwamara@dwitech.com)
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
 * limitations under the License.
 */
package com.dwitech.eap.consulee;

import javax.ejb.ApplicationException;

/**
 * This exception indicates that the Consul configuration is erroneous.
 *
 * @author Ivar Grimstad (ivar.grimstad@gmail.com)
 */
@ApplicationException
public class ConsulConfigurationException extends RuntimeException {

    public ConsulConfigurationException() {
    }

    public ConsulConfigurationException(String message) {
        super(message);
    }

    public ConsulConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsulConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConsulConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
