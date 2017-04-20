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

public class Check {
    private String CheckID;
    private String Name;
    private String Node;
    private String Notes;
    private String Output;
    private String ServiceID;
    private String ServiceName;
    private String Status;

    public Check() {
    }

    public String getCheckID() {
        return this.CheckID;
    }
    public void setCheckID(String CheckID) {
        this.CheckID = CheckID;
    }

    public String getName() {
        return this.Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }

    public String getNode() {
        return this.Node;
    }
    public void setNode(String Node) {
        this.Node = Node;
    }

    public String getNotes() {
        return this.Notes;
    }
    public void setNotes(String Notes) {
        this.Notes = Notes;
    }

    public String getOutput() {
        return this.Output;
    }
    public void setOutput(String Output) {
        this.Output = Output;
    }

    public String getServiceID() {
        return this.ServiceID;
    }
    public void setServiceID(String ServiceID) {
        this.ServiceID = ServiceID;
    }

    public String getServiceName() {
        return this.ServiceName;
    }
    public void setServiceName(String ServiceName) {
        this.ServiceName = ServiceName;
    }

    public String getStatus() {
        return this.Status;
    }
    public void setStatus(String Status) {
        this.Status = Status;
    }
}