#!/bin/sh

ansible-playbook deploy_playbook.yml -vvvv -c ssh --private-key=~/.vagrant.d/insecure_private_key
