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

/**
 * Singleton to store the information gathered from annotation scan.
 * @since 1.0.0
 */
public final class ConsulExtensionHelper {
    private boolean consulEnabled;
    private String serviceName;

    private static final ConsulExtensionHelper INSTANCE = new ConsulExtensionHelper();

    public static String getServiceName() {
        return INSTANCE.serviceName;
    }
    public static void setServiceName(String serviceName) {INSTANCE.serviceName = serviceName; }

    public static boolean isConsulEnabled() { return INSTANCE.consulEnabled; }
    public static void setConsulEnabled(final boolean consulEnabled) { INSTANCE.consulEnabled = consulEnabled; }
}