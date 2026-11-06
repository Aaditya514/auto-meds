#!/usr/bin/env bash
# ==============================================================================
# Auto-Meds Free Let's Encrypt SSL Configuration Script for AWS EC2
# Usage: ./deploy/setup-ssl.sh your-domain.com [email]
# ==============================================================================

set -euo pipefail

if [ -z "${1:-}" ]; then
    echo "Usage: ./deploy/setup-ssl.sh <your-domain.com> [your-email@example.com]"
    echo "Example: ./deploy/setup-ssl.sh automeds.duckdns.org admin@gmail.com"
    exit 1
fi

DOMAIN="$1"
EMAIL="${2:-admin@$DOMAIN}"

echo "=========================================================="
echo " Setting up Let's Encrypt SSL for domain: $DOMAIN"
echo "=========================================================="

# Install Certbot
sudo apt-get update -y
sudo apt-get install -y certbot

# Stop frontend container temporarily to free port 80 for standalone verification
echo "Temporarily stopping frontend container to acquire port 80..."
docker compose stop frontend || true

# Obtain Certificate
echo "Acquiring certificate from Let's Encrypt..."
sudo certbot certonly --standalone \
    -d "$DOMAIN" \
    --agree-tos \
    --email "$EMAIL" \
    --non-interactive

# Copy certificates into deploy/ssl directory
mkdir -p deploy/ssl
sudo cp -L "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" deploy/ssl/fullchain.pem
sudo cp -L "/etc/letsencrypt/live/$DOMAIN/privkey.pem" deploy/ssl/privkey.pem
sudo chmod 644 deploy/ssl/fullchain.pem
sudo chmod 600 deploy/ssl/privkey.pem

echo "Restarting frontend container..."
docker compose up -d frontend

echo "=========================================================="
echo " SSL Certificate successfully provisioned for $DOMAIN!"
echo " Certificates copied to: deploy/ssl/"
echo "=========================================================="

