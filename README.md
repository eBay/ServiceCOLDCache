# Service COLD cache

The Service COLD cache is a CML tool that is designed to run on a developer workstation and speed up application startup time on that workstation. One of the key reasons for slow application startup is service call latency. Applications invoke several remote service calls during intialization, which becomes slower the further you are from ebay data centers. For example, a typical SOA service call is 4-5 slower in Shanghai/Bangalore than from San Jose. The Service COLD cache acts as a local service cache that can cache response from service invocations to local storage. Subsequent repeat service requests are served from the local cache instead of making an invocation to the data center. 

##Main features:

* Cache for http/https request
* Disable/Enable cache at runtime
* Configurable (e.g.customize exclude rules for services don't use cache)

##Downloads

* Download [Latest Release](Downloads/)
* You can also find each released versions [HERE](Downloads/)

##Documentation

- [Setup](docs/setup-standalone.md)
- [Cookbook](docs/cookbook.md)
- [Configurations](docs/configurations.md)
- [Examples](doc/Examples.md)
- [FAQ](docs/faq.md)

##Release Notes

- See [ReleaseNote.md](docs/ReleaseNote.md) for details

##Contributing

- See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for details on how to contribute code.

##Maintainers
* [Patrick Ouyang](mailto:bouyang@ebay.com)
* [Caddie Song](mailto:dsong@ebay.com)

##Contact Us

Email: [DL-eBay-ServiceCOLDCache@ebay.com](mailto:DL-eBay-ServiceCOLDCache@ebay.com)

##License

  [LICENSE](LICENSE)
