#!/bin/sh

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

HOSTS=hosts

function full_provision() {
    ansible-playbook playbook_prod.yml -i $HOSTS --extra-vars "config_dir=config redeploy=full nginx_redeploy=yes" -c ssh -u ansible --private-key=~/.ssh/id_rsa
}

function vagrantRebuild() {
    vagrant destroy -f && vagrant up --provision-with shell
}

function app_redeploy() {
    ansible-playbook playbook_prod.yml -i $HOSTS --extra-vars "redeploy=app nginx_redeploy=no" -c ssh -u ansible --private-key=~/.ssh/id_rsa
}

function default_provision(){
    echo "=> Runing in default interactive mode"
    build_dist
    ansible-playbook playbook_prod.yml -i $HOSTS -c ssh -u ansible --private-key=~/.ssh/id_rsa
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