#! /usr/bin/bash
# - To be executed in the root of the build tree
# - Cleans the directory tree for storing
#   both the chunks and the restored files of
#   either a single peer, in which case you may or not use the argument
#   or for all peers, in which case you

argc=$#
if ((argc == 1))    # removes specific peer folder by peerID
then
  peer_id=$1
  rm -rf ../../resources/peer$peer_id
else
  if ((argc == 0))  # removes all peer storage folders by omission of args
  then
    rm -rf ../../resources/peer*
  else              # print cleanup script usage
    echo "Usage: $0 [<peer_id>]"
    exit 1
  fi
fi





# For a crash course on shell commands check for example:
# Command line basi commands from GitLab Docs':	https://docs.gitlab.com/ee/gitlab-basics/command-line-commands.html
# For shell scripting try out the following tutorials of the Linux Documentation Project
# Bash Guide for Beginners: https://tldp.org/LDP/Bash-Beginners-Guide/html/index.html
# Advanced Bash Scripting: https://tldp.org/LDP/abs/html/
