#!/bin/bash

if (( EUID != 0 )); then
    echo '✗ This script must be run as root' 1>&2
    exit 1
fi

# selinux/firewalld
setenforce 0
sed -i 's/SELINUX=enforcing/\SELINUX=permissive/' /etc/selinux/config
getenforce
systemctl stop firewalld
systemctl disable firewalld

# docker/docker-compose

if docker compose version && docker --version; then
    true
else
   mkdir -p /etc/docker
   mkdir -p /data/docker
   mkdir -p /usr/local/lib/docker/cli-plugins

   cp -rf docker/bin/* /usr/local/bin/
   cp -rf docker/docker.service /usr/lib/systemd/system/
   cp -rf docker/daemon.json /etc/docker/
   if [ ! -e "/usr/bin/docker" ];then
       ln -s /usr/local/bin/docker /usr/bin/
   fi
   if [ ! -e "/usr/local/lib/docker/cli-plugins/docker-compose" ]; then
       ln -s /usr/local/bin/docker-compose /usr/local/lib/docker/cli-plugins/
   fi

   systemctl daemon-reload
   systemctl enable docker
   systemctl start docker
fi

# load container images
find ./images -name '*.tar.gz' -print | xargs -l docker load -i

# start containers
touch .env.local
# check license
source .env.local
while [ -z $SELFHOST_LICENSE ] ||  [ ${#SELFHOST_LICENSE} -lt 128 ];
do
    read -p "Enter the license value: " SELFHOST_LICENSE
    echo "SELFHOST_LICENSE=$SELFHOST_LICENSE" >> .env.local
done 

# merge env
cat .env.template .env.local > .env

docker compose down -v --remove-orphans
for i in {1..3}; do
    echo "${i}" > /dev/null
    if docker compose up -d; then
        break
    fi
    sleep 6
done
