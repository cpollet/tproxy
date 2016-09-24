#!/bin/bash

cat <(printf "GET / HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n") - | nc localhost 8080
