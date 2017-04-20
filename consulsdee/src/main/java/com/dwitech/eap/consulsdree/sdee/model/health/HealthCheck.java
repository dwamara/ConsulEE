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

import java.util.ArrayList;
import java.util.List;

public class HealthCheck {
    private List<Check> Checks = new ArrayList();
    private Node Node;
    private Service Service;

    public HealthCheck() {
    }

    public List<Check> getChecks() {
        return this.Checks;
    }
    public void setChecks(List<Check> Checks) {
        this.Checks = Checks;
    }

    public Node getNode() {
        return this.Node;
    }
    public void setNode(Node Node) {
        this.Node = Node;
    }

    public Service getService() {
        return this.Service;
    }
    public void setService(Service Service) {
        this.Service = Service;
    }
}