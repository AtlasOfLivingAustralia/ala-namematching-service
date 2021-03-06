#!/usr/bin/env bash

SERVER_PATH="target/ala-namematching-server-1.1-SNAPSHOT.jar"
SERVER_URL="http://localhost:9179"

# shutdown if still running
if [ -f "target/pid" ]
then
  pid=$(cat target/pid)
  echo "stopping $pid"
  result=$(rm target/pid)
  result=$(exec kill -9 $pid)
fi

if [ "$1" = "start" ]
then
  echo "starting $SERVER_PATH"
  exec java -jar $SERVER_PATH server config-local.yml > /dev/null 2>&1 & echo "$!" > target/pid

  echo "wait for startup"
  result=""
  maxRetry=20
  while [ "$result" != "true" ] && [ "$result" != "false" ] && [ maxRetry > 0 ]
  do
    echo "waiting for server to start ($maxRetry) ..."
    result=$(curl -s "$SERVER_URL/api/check?name=Animalia&rank=kingdom")
    sleep 1
    maxRetry=$(( $maxRetry - 1 ))
  done

  if [ maxRetry > 0 ]
  then
    echo "Server started"
  else
    echo "Server not yet started"
  fi
  rm "0"  # I don't know what creates this file

elif [ "$1" = "stop" ]
then
  # shutdown already attempted
  echo "stopped"
else
  echo "running $SERVER_PATH"
  exec java -jar $SERVER_PATH server config-local.yml
fi
