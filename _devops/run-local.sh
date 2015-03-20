#!/bin/sh

#  To run deploy against newly created remote host you need to
#  1. create <user>@<host> via adduser
#  2. visudo and add <user>  ALL=NOPASSWD: ALL at the end of the file [it is needed for remote ssh command execution]
#  3. ssh-copy-id -i <your_public_key> <user>@<host>

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

cd $DIR

vagrant destroy -f && vagrant up

cd $DIR/..

sbt clean run