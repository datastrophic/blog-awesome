#!/bin/sh

#  To run deploy against newly created remote host you need to
#  1. create <user>@<host> via adduser
#  2. visudo and add <user>  ALL=NOPASSWD: ALL at the end of the file [it is needed for remote ssh command execution]
#  3. ssh-copy-id -i <your_public_key> <user>@<host>

# In case you  run blog-awesome in the cloud and cloud-config is supported, you can use next sample:
#cloud-config
#users:
#  - name: ansible
#    ssh-authorized-keys:
#      - <contents of your ~/.ssh/id_rsa.pub>
#    sudo: ['ALL=(ALL) NOPASSWD:ALL']
#    groups: sudo
#    shell: /bin/bash


DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

function full_provision() {
#    ansible-playbook playbook_prod.yml --extra-vars "config_dir=config redeploy=full nginx_redeploy=yes" -vvvv -c ssh -u vagrant --private-key=~/.vagrant.d/insecure_private_key
    ansible-playbook playbook_prod.yml -i hosts.prod --extra-vars "config_dir=config redeploy=full nginx_redeploy=yes" -vvvv -c ssh -u ansible --private-key=~/.ssh/id_rsa
}

function vagrantRebuild() {
    vagrant destroy -f && vagrant up
}

function app_redeploy() {
#    ansible-playbook playbook_prod.yml --extra-vars "redeploy=app nginx_redeploy=no" -vvvv -c ssh -u vagrant --private-key=~/.vagrant.d/insecure_private_key
ansible-playbook playbook_prod.yml -i hosts.prod --extra-vars "redeploy=app nginx_redeploy=no" -vvvv -c ssh -u ansible --private-key=~/.ssh/id_rsa
}

function default_provision(){
    echo "=> Runing in default interactive mode"
    build_dist
#    ansible-playbook playbook_prod.yml -vvvv -c ssh -u vagrant --private-key=~/.vagrant.d/insecure_private_key
    ansible-playbook playbook_prod.yml -i hosts.prod -vvvv -c ssh -u ansible --private-key=~/.ssh/id_rsa
}

function build_dist(){
    cd $DIR/.. && sbt clean dist
    cd $DIR
}

while getopts "fpav" opt; do
  case $opt in
    f)
        echo "=> Running dist rebuild and full provision"
        build_dist
        full_provision
        exit 0
        ;;
    p)
        echo "=> Running full provision without dist rebuild"
        full_provision
        exit 0
        ;;
    a)
        echo "=> Running app redeploy only (rebuilding app)"
        build_dist
        app_redeploy
        exit 0
        ;;

    v)
        echo "=> Recreating Vagrant machine and deploying from scratch"
        vagrantRebuild
        build_dist
        full_provision
        exit 0
        ;;
    *)
        break
        ;;
  esac
done

default_provision