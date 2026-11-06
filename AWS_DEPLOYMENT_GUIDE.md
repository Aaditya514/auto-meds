# Auto-Meds AWS Free Tier Deployment Guide

This guide walks you through deploying the complete **Auto-Meds** application (**Angular 16 Frontend + Spring Boot 3 Backend with Tesseract OCR + PostgreSQL 16**) on the **AWS 12-Month Free Tier** for **$0.00 / month**.

---

## 1. AWS Free Tier Cost Breakdown ($0/mo)

| AWS Resource | Free Tier Allowance | Auto-Meds Usage | Cost |
| :--- | :--- | :--- | :--- |
| **EC2 Compute** | 750 hours / month of `t2.micro` (or `t3.micro` in supported regions) | 1 instance running 24/7 (744 hrs max/mo) | **$0.00** |
| **EBS Storage** | 30 GB General Purpose (gp2/gp3) SSD | 25 GB – 30 GB root volume | **$0.00** |
| **Data Transfer** | 100 GB / month outbound to the internet | ~2–5 GB / month for typical usage | **$0.00** |
| **Total Cost** | — | — | **$0.00 / month** |

> [!IMPORTANT]
> **The 1 GB RAM Challenge & Solution**:
> EC2 `t2.micro` and `t3.micro` instances provide 1 vCPU and **1 GB of RAM**. Spring Boot 3 + native Tesseract OCR + PostgreSQL can exceed 1 GB during startup.
> Our automated setup script configures a **4 GB Swapfile** on the free 30 GB EBS volume and tunes the JVM with `-Xms128m -Xmx384m`. This guarantees rock-solid stability without crashing from Out Of Memory (OOM) errors.

---

## 2. Step-by-Step Deployment Instructions

### Step 1: Setup AWS Billing Alert (Zero-Cost Safety Net)
1. Sign in to the [AWS Management Console](https://console.aws.amazon.com/).
2. In the top search bar, type **Budgets** and click **AWS Budgets**.
3. Click **Create budget** $\rightarrow$ select **Zero spend budget**.
4. Set your notification email to receive an alert if your monthly forecast exceeds **$0.01**.

---

### Step 2: Launch the EC2 Free Tier Instance
1. In the AWS Console, search for **EC2** and click **Launch Instance**.
2. **Name**: `automeds-server`
3. **Application and OS Images (Amazon Machine Image)**:
   - Select **Ubuntu**.
   - Choose **Ubuntu Server 24.04 LTS (HVM), SSD Volume Type** (marked *"Free tier eligible"*).
4. **Instance Type**:
   - Select **t2.micro** (1 vCPU, 1 GiB Memory) or **t3.micro** (if `t2.micro` is unavailable in your region).
5. **Key pair (login)**:
   - Click **Create new key pair**.
   - Name: `automeds-key`.
   - Key pair type: **RSA**.
   - Private key file format: **.pem** (for OpenSSH / macOS / Linux / Windows 10/11 PowerShell) or **.ppk** (for PuTTY).
   - Click **Create key pair** and save the downloaded file to a safe location (e.g. `C:\Users\<username>\.ssh\` or `Downloads`).
6. **Network Settings**:
   - Check **Allow SSH traffic from** $\rightarrow$ **Anywhere (0.0.0.0/0)** (or My IP for higher security).
   - Check **Allow HTTP traffic from the internet** (Port 80).
   - Check **Allow HTTPS traffic from the internet** (Port 443).
7. **Configure Storage**:
   - Change volume size from `8 GiB` to **25 GiB** or **30 GiB** (up to 30 GiB is 100% Free Tier eligible).
   - Volume Type: **gp3** or **gp2**.
8. Click **Launch Instance**.

---

### Step 3: Connect to Your EC2 Instance via SSH

Open **PowerShell** (Windows) or **Terminal** (macOS/Linux) and run:

```powershell
# Set strict permissions on the key pair (Windows PowerShell)
icacls "path\to\automeds-key.pem" /inheritance:r
icacls "path\to\automeds-key.pem" /grant:r "$($env:USERNAME):(R)"

# Connect to the instance (replace with your EC2 Public IPv4 address or DNS)
ssh -i "path\to\automeds-key.pem" ubuntu@<YOUR-EC2-PUBLIC-IP>
```

---

### Step 4: Run the Auto-Provisioning Script

Once logged into your Ubuntu EC2 instance, run this single command to configure the 4GB Swap space, Docker Engine, Docker Compose v2, and system firewall:

```bash
# Clone the repository
git clone https://github.com/Aaditya514/auto-meds.git
cd auto-meds

# Make the setup script executable and run it
chmod +x deploy/aws-ec2-setup.sh
./deploy/aws-ec2-setup.sh

# Apply the docker group permissions
newgrp docker
```

---

### Step 5: Configure Environment Variables

Create your production `.env` file from the provided template:

```bash
cp .env.example .env
nano .env
```

Set the production credentials:
1. `POSTGRES_PASSWORD`: Choose a secure password (e.g. `AutoMedsSecurePass2026!`).
2. `DB_PASSWORD`: Set identical to `POSTGRES_PASSWORD`.
3. `JWT_SECRET`: Generate a random 64-character secret using:
   ```bash
   openssl rand -hex 32
   ```
4. `CORS_ALLOWED_ORIGINS`: Set to your EC2 public IP, custom domain, or `*` (e.g. `http://<YOUR-EC2-PUBLIC-IP>`).
5. `SPRING_LIQUIBASE_ENABLED`: Kept as `true` (runs automated Liquibase Formatted SQL migrations on boot).
6. Optional: Add your `NVIDIA_API_KEY` for Nemotron OCR or Gmail SMTP credentials.
7. Save and exit (`Ctrl+O`, `Enter`, `Ctrl+X`).

---

### Step 6: Build & Start Auto-Meds

Run Docker Compose to build and start all 3 containers in detached mode:

```bash
docker compose up --build -d
```

> [!NOTE]
> The first build compiles the Java 17 backend and Angular 16 frontend inside Docker. On a `t2.micro` with 4GB swap, this takes approximately **6 to 10 minutes**. Once built, future starts take less than 5 seconds.

#### Monitor the Launch Progress:
```bash
# Check container status and health probes
docker compose ps

# Follow application logs
docker compose logs -f backend
```

Once the backend reports `Started AutoMedsBackendApplication in ... seconds` and health probes report `(healthy)`, Auto-Meds is live!

---

### Step 7: Access the Web Application

Open your browser and navigate to:
```
http://<YOUR-EC2-PUBLIC-IP>
```

- **Frontend Patient/Pharmacist Portal**: `http://<YOUR-EC2-PUBLIC-IP>/`
- **Backend Actuator Health**: `http://<YOUR-EC2-PUBLIC-IP>/actuator/health`
- **Backend API**: `http://<YOUR-EC2-PUBLIC-IP>/api/`

---

## 3. (Optional) Custom Domain & Free SSL HTTPS

To access your app over HTTPS (`https://yourdomain.com` or `https://automeds.duckdns.org`):

### Option A: Free Dynamic DNS with DuckDNS
1. Go to [duckdns.org](https://www.duckdns.org) and log in.
2. Create a free domain name (e.g., `my-automeds.duckdns.org`).
3. Point its IP address to your **EC2 Public IPv4**.

### Option B: Automated Free Let's Encrypt SSL Script
Run the bundled SSL setup script on your EC2 instance:
```bash
chmod +x deploy/setup-ssl.sh
./deploy/setup-ssl.sh your-domain.com your-email@gmail.com
```
The script stops Nginx, requests a verified certificate from Let's Encrypt, installs the certs into `deploy/ssl/`, and restarts the frontend container.

---

## 4. Useful Operations & Maintenance Commands

| Action | Command |
| :--- | :--- |
| **Check running containers** | `docker compose ps` |
| **View real-time logs** | `docker compose logs -f` |
| **Restart services** | `docker compose restart` |
| **Stop entire stack** | `docker compose down` |
| **Pull code updates & redeploy** | `git pull && docker compose up --build -d` |
| **Check memory & swap usage** | `free -h` or `htop` |

---

## 5. Free Tier Golden Rules: Avoid Accidental Charges

1. **Only run 1 EC2 instance at a time**: The 750 free hours cover 1 instance running 24/7/365. Running 2 instances concurrently consumes 1,500 hours and will be billed.
2. **Do NOT allocate an unattached Elastic IP**: AWS charges for Elastic IPs that are allocated but not attached to a running instance. You can simply use the free standard Public IPv4 address.
3. **Keep total EBS volume $\le$ 30 GB**: You get 30 GB free across all EBS volumes. Stick to a single 25-30 GB root volume.
4. **Avoid AWS Application Load Balancers (ALBs)**: ALBs cost ~$16/month. Auto-Meds already has an internal Nginx reverse proxy serving both the frontend and backend on port 80 for free.

---

## 6. Automated CI/CD Pipeline (GitHub Actions)

A complete continuous integration and automated deployment workflow is configured in [`.github/workflows/ci-cd.yml`](file:///c:/Users/KIIT/Desktop/auto-meds/.github/workflows/ci-cd.yml).

### CI/CD Workflow Phases:
1. **Backend CI**: Spins up a PostgreSQL 16 service, installs Tesseract OCR, runs all JUnit tests with Liquibase migrations, and uploads a JaCoCo coverage report.
2. **Frontend CI**: Sets up Node 18, installs dependencies, and compiles the production Angular 16 distribution.
3. **Docker Verification**: Validates both backend and frontend multi-stage Dockerfiles.
4. **Automated CD Deployment**: On git push to `main`, logs into your AWS EC2 instance via SSH, pulls new code, rebuilds containers with `docker compose up --build -d`, and prunes old images.

### Enabling Automated CD Deployment in GitHub:
Go to your GitHub repository $\rightarrow$ **Settings** $\rightarrow$ **Secrets and variables** $\rightarrow$ **Actions** $\rightarrow$ **New repository secret**:

| Secret Name | Value |
| :--- | :--- |
| `EC2_HOST` | Your EC2 Public IPv4 address (e.g. `54.210.12.34`) |
| `EC2_USER` | `ubuntu` |
| `EC2_SSH_KEY` | Entire content of your `automeds-key.pem` private key file |


