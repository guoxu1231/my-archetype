#!/usr/bin/env bash

du -sh /opt/* | sort -h

# Use it in the folder and list hidden directories size
du -sch .[!.]* * |sort -h