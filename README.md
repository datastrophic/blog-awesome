# Blog Awesome
### Overview
**blog-awesome** is yet another blog engine built with performance in mind, implemented in Scala and ready-to-scale. The stack consists of Play! Framework and Couchbase as main layers and includes deployment scripts that allow to easily deploy your brand new blog to dedicated Ubuntu-based server. 

### Software used
Here is the list of technologies being used to build **blog-awesome**:
* **Frontend**:
 - [SecureSocial][5] for OAuth authentication
 - [WebJars][6] for JS and CSS dependecies management
 - [SirTrevor][7] - the awesomest WYSIWYG JS editor
 - [Twitter Bootstrap][8] of course
* **Backend**:
 * [Play! Framework][1] as the core web framework written in Scala
 * Scala itself with [SpringScala][2] for bean wiring
 * [Couchbase][3] NoSQL document-oriented store 
 * [Nginx][4] for static content serving
* **DevOps**
 * [Ansible][4a] for dev and prod environments provisioning
 * [Docker][4b] [**TBD**]
 * [DataDog][4c] for monitoring

### Quick Start
Next software is needed in order to check out **blog-awesome** at your local machine
- Vagrant 1.7.2 [downloads][d1]
- [VirtualBox][d2]
- SBT 0.13.6 [downloads][d3]
- Ansible 1.8.2 [downloads][d4]

Once all software installed clone the repo
```
git clone git@github.com:akirillov/blog-awesome.git
```
Run `vagrant up` from `_devops` directory in a cloned project. After virtual machine is created run `sbt run` from project root directory.

Head to [localhost:9000](http://localhost:9000) with your browser and login with Facebook or Twitter.

Set access rights for your account in order to create/read/update/delete posts with **blog-awesome**. Head to [192.168.100.10:8091](http://192.168.100.10:8091) Couchbase web console and login with default `admin:password` creds. Locate your account in `users` bucket and change `isAdmin` field to `true`. (_automation for this step is not yet available_)

Relogin to **blog-awesome** at [localhost:9000](http://localhost:9000): you now have new controls to manage your posts and start blogging.

### Development environment and production deployment
In case you're totally new to Play! Framework and Ansible here is some links to start with:
 - Ansible playbooks
 - Vagrant Ansible provisioning
 - Couchbase buckets and views
 - ReactiveCouchbase client which is used 

Hosts provisioning is performed with Ansible and development environment setup is made with help of Vagrant.
If you're already familiar with Vagrant and Ansible, then take a look at set of roles and configurations at `_devops` directory.

#### Configuring development environment

Couchbase server is needed for application development and testing, so it is provided as Vagrant-box provisioned with Ansible.
For starting up the environment with data store `vagrant up` from `_devops` directory and you're ready to run. Vagrantfile uses [playbook_dev.yml] for provisioning, so if any modification needed it has to be made there. Check out that whether everything is fine by running `sbt clean test` from project root directory. There is no convenient way to clean buckets from stale data in case of tests failure for now, the fastest way to clean all the buckets is to login to Couchbase web console and delete all the documents in the buckets.

Packing in Docker container is put on project's road map.

#### Deployment to production

Prior to deployment you have to pack web application in archive with `sbt clean test dist`. You can use provided [octopus_prod.yml] playbook as a reference for production deployment. The main configs you need are located at `_devops/config` directory. All configuration is supposed to be done there. Deployment script `_devops/deploy.sh` contains example for launching `ansible-playbook`. Target hosts should be specified in Ansible inventory file (see `_devops/hosts.awesome`) and passed as argument to `ansible-playbook` command.

 ######Prepare target host:
 * Create [user]@[host] via `adduser`
 * Make him sudo with `visudo` and add `[user]  ALL=NOPASSWD: ALL` at the end of the file (this is needed for remote ssh command execution by Ansible during provisioning)
 * From your localhost `ssh-copy-id -i [your_public_key] [user]@[host]` to allow passwordless ssh for your account

### Configuration details

Each configuration file parameter is then used in Ansible playbooks and templates to build up proper configuration files for services and **blog-awesome** itself.

Overview of configuration files located in `_devops/config` directory: 
* `application.yml` - configuration parameters for Play application itself
* `couchbase.yml` - Couchbase cluster, buckets and views configuration. Check out `design_docs` section - it describes what files located at couchbase role' `files` contain JavaScript for map-reduce description and to what bucket they should be applied
```
design_docs:
  - docname: blog_mr
    file: post_views.json
    buckets:
      - posts
  - docname: blog_mr
    file: comment_views.json
    buckets:
      - comments
 ```
* `datadog.yml` - you DataDog key for setting up proper monitoring and metrics domain to use with dashboards
* `nginx.yml` contains only domain name and path to data directory from where static content is served
* `securesocial.yml` contains `id's` and `tokens` for each supported provider. The values provided are targeted at localhost and must be modified in case you're going to prod. Here is SecureSocial [docs](http://securesocial.ws/guide/configuration.html)
 
 
 [1]: https://www.playframework.com/
 [2]: https://github.com/spring-projects/spring-scala
 [3]: http://www.couchbase.com/
 [4]: http://nginx.org/
 [4a]: http://www.ansible.com/
 [4b]: https://www.docker.com/
[4c]: http://www.datadoghq.com/
 [5]: http://securesocial.ws/
 [6]: http://www.webjars.org/
 [7]: http://madebymany.github.io/sir-trevor-js/
 [8]: http://getbootstrap.com/
 [d1]: https://www.vagrantup.com/downloads.html
 [d2]: https://www.virtualbox.org/
 [d3]: http://www.scala-sbt.org/download.html
 [d4]: http://docs.ansible.com/intro_installation.html
