#!/usr/bin/env bash


#Note: sudo with password
#-S causes sudo to read the password from the standard input instead of the terminal device.
#echo <password> | sudo -S <command>
echo welcome1 | sudo -S pwd

#EE: http://askubuntu.com/questions/147241/execute-sudo-without-password
#edit sudoers file
sudo pwd

#EE: default editor
sudo update-alternatives --config editor