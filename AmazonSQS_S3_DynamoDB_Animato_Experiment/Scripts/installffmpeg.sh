clear
sudo apt-get update
echo "Installing ffmpeg"
sudo add-apt-repository ppa:mc3man/trusty-media
sudo apt-get update
sudo apt-get dist-upgrade
sudo apt-get install ffmpeg
echo "Installed ffmpeg"
exit 1
