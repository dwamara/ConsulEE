# ConsulSDREE - Consul Service Registration and Discovery for Java EE

Inspired by [SnoopEE](https://github.com/ivargrimstad/snoopee/tree/master/snoopee-discovery) and [Spring Cloud Consul](https://cloud.spring.io/spring-cloud-consul/), ConsulSDREE provides the means to register and discover services in [Consul](https://www.consul.io) through annotations.

## Getting Started
### Prerequisites
1. JDK 1.8+
2. Consul
3. one JavaEE 7 certified application server 

### Tested configurations
1. JDK versions
	* [x] 1.8
2. Application servers
	* [x] TomEE [plus-7.0.3]()
	* [x] Wildfly [10.1.0]()
	* [x] Wildfly [11.0.0-alpha]()
	* [ ] Glassfish
3. Consul
	* [x] [0.8.1](https://releases.hashicorp.com/consul/0.8.1/consul_0.8.1_darwin_amd64.zip?_ga=1.102088257.1172276436.1490132183) (running in Docker)
4. Operating Systems
	* [x] macOS 10.12.4
	* [ ] Linux
	* [ ] Windows


## Maven (not yet on Maven Central)
### ConsulSREE - Consul Service Registration

```
<dependency>
    <groupId>com.dwitech.eap.consulsdree</groupId>
    <artifactId>consulsree</artifactId>
    <version>${version.consulsdree}</version>
</dependency>
```

### ConsulSDEE - Consul Service Discovery

```
<dependency>
    <groupId>com.dwitech.eap.consulsdree</groupId>
    <artifactId>consulsdee</artifactId>
    <version>${version.consulsdree}</version>
</dependency>
```

### Dependencies
1. [OrbitzWorldwide/consul-client](https://github.com/OrbitzWorldwide/consul-client)


## TODO

- Add the root path of services as key/pair values in Consul

Implementation profiled with the help of ![Jprofiler](https://www.ej-technologies.com/images/product_banners/jprofiler_large.png) Open Source License
