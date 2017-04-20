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
package com.dwitech.eap.consulsdree.sdee.model.health;

public class Node {
    private String Address;
    private String Node;

    public Node() {
    }

    public String getAddress() {
        return this.Address;
    }
    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getNode() {
        return this.Node;
    }
    public void setNode(String Node) {
        this.Node = Node;
    }
}