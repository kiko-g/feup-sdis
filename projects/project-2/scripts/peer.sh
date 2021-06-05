#bin/bash

echo "0:" "$0"
echo "1:" "$1"
echo "2:" "$2"
echo "3:" "$3"
echo "4:" "$4"
echo "5:" "$5"
echo "6:" "$6"
echo "7:" "$7"

argc=$#

if ((argc < 5))
then
  echo "Usage: bash $0  <protocol_version> <peer_id> <service_access_point> <ip_address> <TCP_port> [<ip_address_of_other> <TCP_port_of_other>]"
  echo "Use the last 2 arguments only if you're NOT launching a peer initiator"
  exit 1
fi

version=$1
peer_id=$2
peer_ap=$3
ip_address=$4
tcp_port=$5

cd src/build/

if ((argc == 5))
then
    java Peer "${version}" "${peer_id}" "${peer_ap}" "${ip_address}" "${tcp_port}" 
fi

if ((argc == 7))
then
    ip_address_other=$6
    tcp_port_other=$7
    java Peer "${version}" "${peer_id}" "${peer_ap}" "${ip_address}" "${tcp_port}" "${ip_address_other}" "${tcp_port_other}"
fi