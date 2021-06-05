#!/bin/sh
base=$(basename "$(pwd)")

if [ "$base" = "test" ]; then
  java -cp ../bin/ Peer "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8"
else
  java -cp bin/ Peer "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8"
fi

# bash peer.sh 1923 peer1 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323
# bash peer.sh 1924 peer2 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323
# bash peer.sh 1925 peer3 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323
