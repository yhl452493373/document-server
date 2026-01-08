#!/bin/bash
echo installing fonts
sudo mkdir -p /usr/share/fonts/truetype
sudo mkdir -p /usr/share/fonts/cesi
sudo mkdir -p /usr/share/fonts/gb
sudo mkdir -p /usr/share/fonts/wps-office
sudo cp -r ./fonts/truetype/* /usr/share/fonts/truetype/
sudo cp -r ./fonts/cesi/* /usr/share/fonts/cesi/
sudo cp -r ./fonts/gb/* /usr/share/fonts/gb/
sudo cp -r ./fonts/wps-office/* /usr/share/fonts/wps-office/
sudo fc-cache -fv
echo fonts installed
