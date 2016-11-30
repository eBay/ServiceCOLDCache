## Contributing Code

This project takes all contributions through
[pull requests](https://help.github.com/articles/using-pull-requests)(PR).
Code should **not** be pushed directly to `master`.

The following guidelines apply to all contributors.

### Making Changes

* Fork the `DevEx/service-cold-cache` repository
* Make your changes and push them to a topic branch in your fork
  * See our commit message guidelines further down in this document
* Submit a pull request to the `DevEx/service-cold-cache` repository
* Send email to [DL-eBay-ServiceCOLDCache@ebay.com](mailto:DL-eBay-ServiceColdCache@ebay.com) to get review for your PRs.

### General Guidelines


* Only one logical change per commit
* Do not mix whitespace changes with functional code changes
* Do not mix unrelated functional changes
* When writing a commit message:
  * Describe *why* a change is being made
  * Do not assume the reviewer understands what the original problem was
  * Do not assume the code is self-evident/self-documenting
  * Describe any limitations of the current code
* Make sure code achieve Quality Requirements. 
  * Unit test added for code changes and all of the unit tests pass.
  * 80% code coverage
  * 0 critial/blocker violations checking by findbugs, checkstyle and pmd.
  
  
### How to build a product

* Run a mvn clean install in `source folder`, product will be built under `target/dist` folder


### Issues Tracking

* Use [GitHub Issues](https://github.com/eBay/ServiceCOLDCache/issues) to tracking issues, new feature, enhancements.


  
  



