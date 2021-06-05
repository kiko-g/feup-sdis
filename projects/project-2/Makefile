#src/*/*.java src/*/*/*.java
REP_DEG = 1
NEW_SIZE = 0
FILEPATH = "../../file.pdf"
ACCESS_POINT = "Peer1"



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
	find src/build/peer* # ls src/build/peer* -RalS




# PEERS
peer1:
	cd src/build; java Peer 1.0 1 Peer1 127.0.1.1 6001

peer2:
	cd src/build; java Peer 1.0 2 Peer2 127.0.1.2 6002 127.0.1.1 6001

peer3:
	cd src/build; java Peer 1.0 3 Peer3 127.0.1.3 6003 127.0.1.2 6002

peer4:
	cd src/build; java Peer 1.0 4 Peer4 127.0.1.4 6004 127.0.1.3 6003


# CLIENT
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

