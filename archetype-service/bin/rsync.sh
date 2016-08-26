#!/usr/bin/env bash

rsync -avz -e "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" --progress /opt/Development/github_repo/my-archetype/archetype-service admin@10.139.52.45:/alidata1/admin