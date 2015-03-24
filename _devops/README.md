# Blog Awesome deployment

### Deployment to production

Helper script `provision.sh` is provided to simplify deployment routine. If you look into script it's just a wrapper 
for Ansible playbooks running and application assembly. Next common use cases are covered with script's 
parameters:
 * `-f` - full rebuild and redeploy
 * `-p` - runs full provision without reassembly of webapp
 * `-a` - reassembly of application and redeploying it without any other roles
 * `-v` - destroys Vagrant machines and runs full provision with reassembly (used for playbooks debugging)
 * running without params invokes interactive Ansible prompt to specify what roles should be deployed
 
Hosts to provision should be specified in `hosts` file.

#####Prepare target host
######shell:
 * Create [user]@[host] via `adduser`
 * Make him sudo with `visudo` and add `[user]  ALL=NOPASSWD: ALL` at the end of the file (this is needed for remote ssh command execution by Ansible during provisioning)
 * From your localhost `ssh-copy-id -i [your_public_key] [user]@[host]` to allow passwordless ssh for your account
######cloud-config:
In case you run blog-awesome in the cloud and cloud-config/user-data is supported, you can use next sample:

        cloud-config
        users:
          - name: ansible
            ssh-authorized-keys:
              - <contents of your ~/.ssh/id_rsa.pub>
            sudo: ['ALL=(ALL) NOPASSWD:ALL']
            groups: sudo
            shell: /bin/bash

####Admin rights for post publishing
When going to prod, after deploy and first login you need to set access rights for your account 
in order to create/read/update/delete posts with **blog-awesome**. Head to Couchbase web console at 
<host_ip>:8091 and login with creds specified in `couchbase.yml` config file. 
Locate your account in `users` bucket and change `isAdmin` field to `true`.

Relogin to **blog-awesome** and you'll have new controls to manage your posts and start blogging.

### Configuration details

Each configuration file parameter is then used in Ansible playbooks and templates to build up proper configuration files for services and **blog-awesome** itself.

Overview of configuration files located in `_devops/config` directory: 

 * `application.yml` - configuration parameters for Play application itself
 * `couchbase.yml` - Couchbase cluster, buckets and views configuration. Check out `design_docs` section - it describes what files located at couchbase role' `files` contain JavaScript for map-reduce description and to what bucket they should be applied
        
        design_docs:
          - docname: blog_mr
            file: post_views.json
            buckets:
              - posts
          - docname: blog_mr
            file: comment_views.json
            buckets:
              - comments
 * `datadog.yml` - you DataDog key for setting up proper monitoring and metrics domain to use with dashboards
 * `nginx.yml` contains only domain name and path to data directory from where static content is served
 * `securesocial.yml` contains `id's` and `tokens` for each supported provider. The values provided are targeted at localhost and must be modified in case you're going to prod. Here is SecureSocial [docs](http://securesocial.ws/guide/configuration.html)

### OAuth/SecureSocial Configuration
When using non-localhost address you should provide proper configuration for SecureSocial to login with
 desired providers (like Facebook or Twitter). Check out appropriate section of [SecureSocial docs](http://securesocial.ws/guide/configuration.html).
 You have to register at each website you want to use as OAuth provider to get appropriate keys.