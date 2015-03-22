# Blog Awesome
### Overview
**blog-awesome** is yet another blog engine built with performance in mind, implemented in Scala and ready-to-scale. The stack consists of Play! Framework and Couchbase as main layers and includes deployment scripts that allow to easily deploy your brand new blog to dedicated Ubuntu-based server. 

### Quick Start
Next software is needed in order to check out **blog-awesome** at your local machine
- Vagrant 1.7.2 [downloads][d1]
- [VirtualBox][d2]
- SBT 0.13.6 [downloads][d3]
- Ansible 1.8.2 [downloads][d4]

Once all software installed clone the repo
        
        git clone git@github.com:akirillov/blog-awesome.git
        
Run `vagrant up` from `_devops` directory in a cloned project. After virtual machine is created run `sbt run` from project root directory.

Head to [localhost:9000](http://localhost:9000) with your browser and login with Facebook or Twitter ('>_' button in navbar).

In local environment you have admin access for any user logged in. It gives rights to create/read/update/delete posts with **blog-awesome**.

### Development environment
In case you're totally new to Play! Framework and Ansible here is some links to start with:
 - [Ansible playbooks][dev1]
 - [Vagrant Ansible provisioning][dev2]
 - Couchbase [buckets][dev3], [views][dev4] and [CLI][dev5] reference
 - [ReactiveCouchbase][dev6] Scala client which is used 

Hosts provisioning is performed with Ansible and development environment setup is made with help of Vagrant.
If you're already familiar with Vagrant and Ansible, then take a look at set of roles and configurations at `_devops` directory.

### Deploying to production

Check out appropriate [**README**](_devops) about DevOps

#### Configuring development environment

Couchbase server is needed for application development and testing, so it is provided as Vagrant-box provisioned with Ansible.
For starting up the environment with data store `vagrant up` from `_devops` directory and you're ready to run. 
Vagrantfile uses [playbook_dev.yml] for provisioning, so if any modification needed it has to be made there. 
Check out that whether everything is fine by running `sbt clean test` from project root directory. 
There is no convenient way to clean buckets from stale data in case of tests failure for now, 
the fastest way to clean all the buckets is to login to Couchbase web console and delete all the documents in the buckets.

Packing in Docker container is on the project's road map (waiting Docker 1.6 with `ulimits` support).
 
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
 * [DataDog][4c] for monitoring
 
 
 [1]: https://www.playframework.com/
 [2]: https://github.com/spring-projects/spring-scala
 [3]: http://www.couchbase.com/
 [4]: http://nginx.org/
 [4a]: http://www.ansible.com/
[4c]: http://www.datadoghq.com/
 [5]: http://securesocial.ws/
 [6]: http://www.webjars.org/
 [7]: http://madebymany.github.io/sir-trevor-js/
 [8]: http://getbootstrap.com/
 [d1]: https://www.vagrantup.com/downloads.html
 [d2]: https://www.virtualbox.org/
 [d3]: http://www.scala-sbt.org/download.html
 [d4]: http://docs.ansible.com/intro_installation.html
 [dev1]: http://docs.ansible.com/playbooks.html
 [dev2]: http://docs.vagrantup.com/v2/provisioning/ansible.html
 [dev3]: http://docs.couchbase.com/admin/admin/UI/ui-data-buckets.html
 [dev4]: http://docs.couchbase.com/admin/admin/Views/views-intro.html
 [dev5]: http://docs.couchbase.com/admin/admin/cli-intro.html
 [dev6]: http://reactivecouchbase.org/
