#bin/bash

argc=$#

if ((argc < 3))
then
  echo "Usage: bash $0 <peer_ap> <filepath> <replication_degree>"
  exit 1
fi

peer_ap=$1
filepath=$2
replication_degree=$3

cd src/build/
java Client "${peer_ap}" BACKUP "${filepath}" "${replication_degree}"
