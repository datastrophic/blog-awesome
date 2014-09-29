Development environment and production deployment
============

Hosts provisioning is performed with Ansible and development environment setup is made with help of Vagrant.

Setting up a development environment
----------

Couchbase server is needed for application development and testing, so it is provided as Vagrant-box provisioned with Ansible.
For starting up a box ```vagrant up``` is enough to get it up and running. Vagrantfile uses [devplaybook.yml] for provisioning,
so if any modification needed it has to be made there.

Deployment to production
----------

You can use provided [deploy_sample.yml] playbook as a base for production deployment. It includes several more steps such as 
  application
