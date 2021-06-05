# Backup Service for the Internet (SDIS P2)

- [Backup Service for the Internet (SDIS P2)](#backup-service-for-the-internet-sdis-p2)
	- [Simple Execution](#simple-execution)
	- [Elaborate Execution](#elaborate-execution)
	- [Makefile structure](#makefile-structure)
		- [1. Global default variables](#1-global-default-variables)
		- [2. Compile and setup](#2-compile-and-setup)
		- [3. Peers](#3-peers)
		- [4. Client](#4-client)
	- [Operations](#operations)

<br>

## Simple Execution

To run the program with default arguments you can try

```bash
make clean      # cleanup build directory (optional)
make            # compile all java files inside src/
make kill       # kill rmi (optional)
make rmi        # run rmiregistry
make peer1      # run peer1 with default arguments
make peer2      # run peer2 with default arguments
make peer3      # run peer2 with default arguments
make backup     # or restore/delete/reclaim/state/chord
make status 	# show storage (optional)
```

## Elaborate Execution

```bash
# setup
make clean # or
make
make kill
make rmi

# launch peers
bash scripts/peer.sh <protocol_version> <peer_id> <service_access_point> <ip_address> <TCP_port> [<ip_address_of_other> <TCP_port_of_other>]

# execute a client operation
bash scripts/backup.sh <peer_access_point> <filepath> <replication_degree>
bash scripts/restore.sh <peer_access_point> <filepath>
bash scripts/restore.sh <peer_access_point> <filepath>
bash scripts/restore.sh <peer_access_point> <new_size>
bash scripts/state.sh <peer_access_point>
bash scripts/chord.sh <peer_access_point>

# check storage with commands like
# this
find src/build/peer*

# or this
ls src/build/peer* -RalS
```

## Makefile structure

The makefile is a swift way of executing a rather complicated program.
You may need to make some changes in the makefile to suit your needs, so take a look at this section for that. The makefile has **4 main sections**:

### 1. Global default variables

```makefile
REP_DEG = 1
NEW_SIZE = 0
FILEPATH = "../../file.pdf"
ACCESS_POINT = "Peer1"
```

If you want to stick with the simple run described [above](#simple-execution), just tweak these variables.

### 2. Compile and setup

```makefile
all: mkdir
	cd src; javac -d build/ *.java

mkdir:
	@mkdir -p src/build/

clean:
	@rm -rf src/build/

rmi:
	cd src/build; rmiregistry &

kill:
	@bash scripts/kill_rmi.sh

status:
	find src/build/peer*
```

These are the commands to compile and setup your environment before executing.

- The `all` option compiles the code into `src/build`
- The `mkdir` entry is just for support and it creates the `src/build` directory
- The `clean` option removes the `src/build`
- The `kill` option gets rid of other `rmiregistry` processes
- The `rmi` option starts an `rmiregistry` process in the background, allowing you to execute the program later.
- The `status` option shows you the state of the storage (peer folders).

### 3. Peers

```makefile
peer1:
	cd src/build; java Peer 1.0 1 Peer1 127.0.1.1 6001

peer2:
	cd src/build; java Peer 1.0 2 Peer2 127.0.1.2 6002 127.0.1.1 6001

peer3:
	cd src/build; java Peer 1.0 3 Peer3 127.0.1.3 6003 127.0.1.2 6002

peer4:
	cd src/build; java Peer 1.0 4 Peer4 127.0.1.4 6004 127.0.1.3 6003
```

This is the section with the commands to launch peers into the program.

- The `peer` option allows you to launch a peer with your own arguments
- The `peer<N>` option (where `N` is the `peerID`) allows to launch a peer with the default values presented. Suitable for simple test runs. If you want more than 4 of these commands copy the last one and create the next `peerN`.
- Initiate peers without makefile or scripts:
  - First peer to join the Chord ring: `java Peer <protocol_version> <peer_id> <service_access_point> <ip_address> <TCP_port>`
  - Initiate the other peers: `java Peer <protocol_version> <peer_id> <service_access_point> <ip_address> <TCP_port> <ip_address_of_other> <TCP_port_of_other>`

### 4. Client

```makefile
backup:
	@bash scripts/backup.sh $(ACCESS_POINT) $(FILEPATH) $(REP_DEG)

restore:
	@bash scripts/restore.sh $(ACCESS_POINT) $(FILEPATH)

delete:
	@bash scripts/delete.sh $(ACCESS_POINT) $(FILEPATH)

reclaim:
	@bash scripts/reclaim.sh $(ACCESS_POINT) $(NEW_SIZE)

state:
	@bash scripts/state.sh $(ACCESS_POINT)

chord:
	@bash scripts/chord.sh $(ACCESS_POINT)
```

This section has the commands that make client requests to the peers with the available operations. These commands use the variables defined in the [first section](#makefile-structure) of the makefile. Change them to alter the behavior of the program.

## Operations

- Backup:
  - Peer asks to save chunks of file on other peers with a replication degree
  - java Client <peer_ap> BACKUP <file_path_name> <replication_degree>
- Restore
  - Peer asks to retrieve the backed up chunks of the file previously backed up
  - java Client <peer_ap> RESTORE <file_path_name>
- Delete
  - Peer asks to delete the previously backed up file
  - java Client <peer_ap> DELETE <file_path_name>
- Reclaim
  - Client decides to change the space available for a given peer
  - java Client <peer_ap> RECLAIM <maximum_disk_space_KB>
- State
  - Shows information about the peer and its storage
  - java Client <peer_ap> STATE
- Chord
  - Log information Chord related
  - java Client <peer_ap> CHORD
