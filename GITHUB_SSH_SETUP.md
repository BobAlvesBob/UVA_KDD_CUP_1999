# GitHub SSH Authentication Setup

## ✅ Step 1: SSH Key Generated

Your SSH key has been successfully generated!

**Public Key:**
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIATWgB/X+0Xrbpdl1FG79TCShHkev9lLj6U4SqiZJyeu usuario@virtual
```

**Files created:**
- Private key: `~/.ssh/id_ed25519` (keep this secret!)
- Public key: `~/.ssh/id_ed25519.pub` (this is what you add to GitHub)

## 📋 Step 2: Add SSH Key to GitHub

1. **Copy the public key** (already shown above, or run):
   ```bash
   cat ~/.ssh/id_ed25519.pub
   ```

2. **Go to GitHub** and add the key:
   - Visit: https://github.com/settings/ssh/new
   - Or navigate: GitHub → Settings → SSH and GPG keys → New SSH key

3. **Add the key:**
   - **Title:** `UVA Virtual Machine` (or any descriptive name)
   - **Key:** Paste the entire public key (starting with `ssh-ed25519...`)
   - Click **Add SSH key**

## 🔄 Step 3: Update Git Remote to Use SSH

Once you've added the key to GitHub, run:

```bash
cd /home/usuario/Datos/proyecto-kdd-cup-1999
git remote set-url origin git@github.com:BobAlvesBob/UVA_KDD_CUP_1999.git
```

## ✅ Step 4: Test Connection

```bash
ssh -T git@github.com
```

You should see: `Hi BobAlvesBob! You've successfully authenticated...`

## 🚀 Step 5: Push to GitHub

```bash
git push -u origin main
```

## Quick Reference

### View current remote:
```bash
git remote -v
```

### Change back to HTTPS (if needed):
```bash
git remote set-url origin https://github.com/BobAlvesBob/UVA_KDD_CUP_1999.git
```

### Change to SSH:
```bash
git remote set-url origin git@github.com:BobAlvesBob/UVA_KDD_CUP_1999.git
```

## Troubleshooting

### "Permission denied (publickey)"
- Make sure you added the key to GitHub
- Verify SSH agent is running: `ssh-add -l`
- If needed, add key again: `ssh-add ~/.ssh/id_ed25519`

### "Host key verification failed"
First time connecting? Run: `ssh-keyscan github.com >> ~/.ssh/known_hosts`

### See your public key again:
```bash
cat ~/.ssh/id_ed25519.pub
```
