#!/bin/sh

#  To run deploy against newly created host you need to
#  1. create <user>@<host> via adduser
#  2. visudo and add <user>  ALL=NOPASSWD: ALL at the end of the file [it is needed for remote ssh command execution]
#  3. ssh-copy-id -i <your_public_key> <user>@<host>

#ansible-playbook playbook_prod.yml -vvvv -c ssh -u vagrant --private-key=~/.vagrant.d/insecure_private_key

ansible-playbook octopus_prod.yml -i prodconfig/flyingoctopus/hosts.octopus -vvvv -c ssh -u ansible --private-key=~/.ssh/id_rsa
