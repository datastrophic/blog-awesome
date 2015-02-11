#### Overview
Blog-Awesome is yet another blog engine built with performance in mind, implemented in Scala and ready-to-scale.
The stack consists of Play! Framework and Couchbase as main layers and includes deployment scripts that allow to
 easily deploy your brand new blog to dedicated ubuntu-based server. 

#### Software used

Backend:
- Play! Scala with SpringScala
- Couchbase
- Nginx for static content

Frontend:
- SecureSocial
- WebJars
- SirTrevor
- Bootstrap

#### Quick Start
Pre-requisites:
    - Vagrant
    - VirtualBox
    - SBT
    - Ansible

Here is quick setup to checkout blog-awesome functionality:
 - git clone
 - start-blog.sh #TODO
 - login to blog with FB or Twitter
 - login to couchbase and change admin: true (automating this one-time operation doesn't makes much sense so it is manual)
 - relogin to blog and start posting

Development environment and production deployment
============

In case you're totally new to Play! Framework and Ansible here is some useful links to start with:
 - playbooks
 - Vagrant Ansible provisioning
 - reference to Couchbase buckets cinfiguration

Hosts provisioning is performed with Ansible and development environment setup is made with help of Vagrant.
If you're familiar with Vagrant and Ansible, then take a look at set of roles and configuraations at `_devops` directory.
 
 SHORT DESCRIPTION


Setting up a development environment
----------

Couchbase server is needed for application development and testing, so it is provided as Vagrant-box provisioned with Ansible.
For starting up the environment with data store `vagrant up` from `_devops` directory and you're ready to run. Vagrantfile uses [playbook_dev.yml.yml] for provisioning,
so if any modification needed it has to be made there. Check out that whether everything is fine by running `sbt clean test` from project
root directory. There is no convenient way to clean buckets from stale data in case of tests failure for now, the fastest way to clean
all the buckets is to login to Couchbase web-ui and delete all the documents in the buckets.

Packing in Docker container is put on project's road map.

Deployment to production
----------

Prior to deployment you have to pack web application in archive with `sbt clean test dist`. 
You can use provided [octopus_prod.yml] playbook as a reference for production deployment. I use it myself for `www.flyingoctopu.io`.
The main configs you need are located at `_devops/config` directory. All configuration is supposed to be done there. 
 
 #  To run deploy against newly created machine/VPS you need to
 #  1. create <user>@<host> via adduser
 #  2. visudo and add <user>  ALL=NOPASSWD: ALL at the end of the file (this is needed for remote ssh command execution by Ansible during provisioning)
 #  3. ssh-copy-id -i <your_public_key> <user>@<host>

Configuration details
----------

#TODO describe each file
 
 Short overview of CB views and buckets configuration
 