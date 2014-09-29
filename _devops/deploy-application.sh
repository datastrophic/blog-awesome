#!/bin/sh

#TODO: add app redeploy parameter
ansible-playbook deploy_playbook.yml -c ssh --private-key=~/.vagrant.d/insecure_private_key
