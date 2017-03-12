clear
sudo apt-get update
echo "Installing pssh"
sudo ln -s /usr/bin/parallel-ssh /usr/local/bin/pssh
sudo apt-get install pssh
echo "Installed pssh"
exit 1
