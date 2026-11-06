#!/usr/bin/env bash
# ==============================================================================
# Auto-Meds AWS EC2 Free Tier Auto-Provisioning Script (Ubuntu 22.04 / 24.04 LTS)
# Configures 4GB Swap, installs Docker Engine + Compose v2, and opens ports.
# ==============================================================================

set -euo pipefail

echo "=========================================================="
echo " Starting Auto-Meds EC2 Free Tier Provisioning"
echo " Target Instance: t2.micro / t3.micro (1 GB RAM + 30GB EBS)"
echo "=========================================================="

# 1. Configure 4GB Swap Space (Essential for Spring Boot 3 + Tesseract on 1GB RAM)
if [ ! -f /swapfile ]; then
    echo "--> [1/4] Configuring 4GB Swapfile to prevent OOM termination..."
    sudo fallocate -l 4G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=4096
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    
    # Tune swappiness for optimal responsiveness
    sudo sysctl vm.swappiness=20
    sudo sysctl vm.vfs_cache_pressure=50
    echo 'vm.swappiness=20' | sudo tee -a /etc/sysctl.conf
    echo 'vm.vfs_cache_pressure=50' | sudo tee -a /etc/sysctl.conf
    echo "Swap configured successfully:"
    free -h
else
    echo "--> [1/4] Swapfile already exists. Skipping."
fi

# 2. Update System and Install Core Utilities
echo "--> [2/4] Updating package indexes and installing core tools..."
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release git ufw htop

# 3. Install Official Docker Engine and Docker Compose Plugin
echo "--> [3/4] Installing Docker Engine and Docker Compose v2..."
if ! command -v docker &> /dev/null; then
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor --yes -o /etc/apt/keyrings/docker.gpg
    sudo chmod a+r /etc/apt/keyrings/docker.gpg

    echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
      $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

    sudo apt-get update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

    # Enable and start Docker service
    sudo systemctl enable docker
    sudo systemctl start docker

    # Add current user to docker group (avoids needing sudo for docker commands)
    sudo usermod -aG docker "$USER"
    echo "Docker installed successfully:"
    docker --version
else
    echo "Docker already installed. Ensuring service is running..."
    sudo systemctl enable --now docker
fi

# 4. Configure Host Firewall (UFW)
echo "--> [4/4] Configuring UFW Firewall (SSH 22, HTTP 80, HTTPS 443)..."
sudo ufw allow 22/tcp comment 'SSH'
sudo ufw allow 80/tcp comment 'HTTP Web Portal'
sudo ufw allow 443/tcp comment 'HTTPS Secure Portal'
sudo ufw --force enable
sudo ufw status verbose

echo "=========================================================="
echo " Auto-Meds EC2 Setup Complete!"
echo " Log out and log back in (or run 'newgrp docker') to apply"
echo " docker group permissions."
echo "=========================================================="

