# Serverless Distributed Backup Service

## Project Description

This project is a serverless backup service written in Java that allows peers to perform operations through the TestApp aka Client.
The available operations are:

- **Backup**:
  - Peer asks to save chunks of file on other peers with a replication degree
- Restore
  - Peer asks to retrieve the backed up chunks of the file previously backed up
- Delete
  - Peer asks to delete the previously backed up file
- Reclaim
  - Client decides to change the space available for a given peer
- State
  - Shows information about one peer and its storage

## Test the project

```shell
# we recommend using one terminal windows for every execution

# navigate to proj1
cd src/
bash ../scripts/compile.sh

# launch peers
cd build/
bash ../../scripts/cleanup.sh
bash ../../scripts/peer.sh 1.0 1 1923 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323
bash ../../scripts/peer.sh 1.0 2 1924 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323
bash ../../scripts/peer.sh 1.0 3 1925 230.0.0.0 4321 230.0.0.1 4322 230.0.0.2 4323 #optional

# then choose one client operation
bash ../../scripts/test.sh 1923 BACKUP ../../resources/file.pdf 2
bash ../../scripts/test.sh 1923 RESTORE ../../resources/file.pdf
bash ../../scripts/test.sh 1923 DELETE ../../resources/file.pdf
bash ../../scripts/test.sh 1923 STATE
bash ../../scripts/test.sh 1924 RECLAIM 0
```

## Alternative Run

```shell
# we recommend using one terminal windows for every execution

# navigate to proj1
cd test/

# compile
./make.sh

# launch peers
./peer1.sh
./peer2.sh
./peer3.sh

# then choose one client operation
./client.sh
./client_delete.sh
./client_restore.sh
./client_reclaim.sh
./client_state.sh
```
