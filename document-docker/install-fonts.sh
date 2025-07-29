#!/bin/bash
echo installing fonts
sudo mkdir -p /usr/share/fonts/truetype
sudo cp -r ./fonts/* /usr/share/fonts/truetype/
sudo fc-cache -fv
echo fonts installed
