#!/bin/sh

ansible-playbook playbook_prod.yml -vvvv -c ssh -u vagrant --private-key=~/.vagrant.d/insecure_private_key
