#bin/bash

argc=$#

if ((argc < 1))
then
  echo "Usage: bash $0 <peer_ap>"
  exit 1
fi

peer_ap=$1

cd src/build/
java Client "${peer_ap}" CHORD