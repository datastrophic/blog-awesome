Blog-Awesome
============

PlayFramework-based blog engine with zero configuration, easy deployment and powerful post editing capabilities 

Overview
-------

Features overview with screenshots and link to post with video

Using Blog-Awesome
-------

TODO: build, configuration and deployment from checkout to working service. Admin rights setup, couchbase views (try to automate) 


Blog-Awesome architecture
------



Building & Testing
-------

Some configuration is needed to run test suite against blog-awesome. 
For simplicity special vagrant file is provided in /path/to/vagrant which allows start up test environment with 
all necessary ecosystem provisioned by Ansible.
 
Note:
- if you're using IntelliJ Idea for development, 
you'll need to specify test configuration file path in VM options: -Dconfig.file=conf/application.test.conf  