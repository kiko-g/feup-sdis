#bin/bash

argc=$#

if ((argc < 2))
then
  echo "Usage: $0 <peer_ap> <filepath>"
  exit 1
fi

peer_ap=$1
filepath=$2

cd src/build/
java Client "${peer_ap}" DELETE "${filepath}"