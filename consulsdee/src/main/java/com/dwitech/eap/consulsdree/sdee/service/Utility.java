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
package com.dwitech.eap.consulsdree.sdee.service;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Utility {
    public Utility() {
    }

    public static String readUrl(String urlString) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader br = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();
            br = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = br.readLine()) != null) {
                result.append(line);
            }
        } finally {
            closeQuitely(inputStream);
            closeQuitely(br);
        }

        return result.toString();
    }

    public static void closeQuitely(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (Exception exc) {
            }
        }
    }
}