# Instruções para Subir no GitHub

## ✅ Git Já Configurado

O repositório Git local já está configurado e o commit inicial foi feito.

**Status atual:**
- ✓ Git inicializado
- ✓ .gitignore configurado (exclui arquivos de dados pesados)
- ✓ Commit inicial feito com 12 arquivos
- ✓ Branch: `main`

## 📤 Passos para Criar o Repositório no GitHub

### Opção A: Via Interface Web do GitHub (Recomendado)

1. **Acessar GitHub:**
   - Ir para: https://github.com/new
   - Fazer login se necessário

2. **Criar Novo Repositório:**
   - **Repository name:** `proyecto-kdd-cup-1999` (ou outro nome)
   - **Description:** `Proyecto de Clasificación - KDD Cup 1999 Dataset - Detección de Intrusiones en Redes`
   - **Visibilidade:** ✅ Public (público)
   - **NÃO** marcar "Add a README file" (já temos)
   - **NÃO** marcar "Add .gitignore" (já temos)
   - **License:** Opcional - MIT ou Apache 2.0 são boas opções
   - Clicar em **"Create repository"**

3. **Copiar a URL do Repositório:**
   - Após criar, o GitHub mostrará uma URL como:
   - `https://github.com/seu-usuario/proyecto-kdd-cup-1999.git`
   - Copiar essa URL

4. **Conectar o Repositório Local ao GitHub:**
   ```bash
   cd /home/usuario/Datos/proyecto-kdd-cup-1999
   
   # Adicionar o remote (substituir URL pela sua)
   git remote add origin https://github.com/SEU-USUARIO/proyecto-kdd-cup-1999.git
   
   # Fazer push do código
   git push -u origin main
   ```

5. **Pronto!** O projeto estará público no GitHub

### Opção B: Via GitHub CLI (gh)

Se tiver o `gh` instalado:

```bash
cd /home/usuario/Datos/proyecto-kdd-cup-1999

# Criar repositório público
gh repo create proyecto-kdd-cup-1999 --public --source=. --remote=origin --push

# Verificar
gh repo view --web
```

## 🔍 Verificar o Que Será Enviado

```bash
cd /home/usuario/Datos/proyecto-kdd-cup-1999

# Ver arquivos que SERÃO enviados
git ls-files

# Ver arquivos que NÃO serão enviados (ignorados)
git status --ignored
```

**Arquivos que SERÃO enviados:**
- ✓ README.md
- ✓ Scripts Scala (*.scala)
- ✓ Documentação (*.md)
- ✓ Referencias (*.bib)
- ✓ .gitignore

**Arquivos que NÃO serão enviados:**
- ✗ datos/raw/*.data (709 MB)
- ✗ datos/raw/*.zip
- ✗ datos/processed/*.parquet
- ✗ Arquivos temporários do Spark

## 📋 Adicionar Badge ao README (Opcional)

Depois de criar o repo, você pode adicionar badges ao README.md:

```markdown
# Projeto KDD Cup 1999

[![GitHub](https://img.shields.io/github/license/seu-usuario/proyecto-kdd-cup-1999)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/seu-usuario/proyecto-kdd-cup-1999)](https://github.com/seu-usuario/proyecto-kdd-cup-1999/stargazers)
```

## 🔐 Configurar Nome e Email do Git (Se Necessário)

Se você quiser usar seu nome e email reais:

```bash
# Global (para todos os projetos)
git config --global user.name "Seu Nome"
git config --global user.email "seu.email@example.com"

# Ou só para este projeto
cd /home/usuario/Datos/proyecto-kdd-cup-1999
git config user.name "Seu Nome"
git config user.email "seu.email@example.com"

# Refazer o commit com o novo autor
git commit --amend --reset-author --no-edit
```

## 🌿 Trabalhar com Branches (Opcional)

Para trabalhar em features sem afetar a branch principal:

```bash
# Criar branch para desenvolvimento
git checkout -b desarrollo

# Fazer mudanças, adicionar e commitar
git add .
git commit -m "Descrição das mudanças"

# Fazer push da nova branch
git push -u origin desarrollo

# Voltar para main
git checkout main
```

## 📦 Futuras Atualizações

Quando fizer mudanças:

```bash
cd /home/usuario/Datos/proyecto-kdd-cup-1999

# Ver o que mudou
git status

# Adicionar mudanças
git add .

# Commitar
git commit -m "Descrição da atualização"

# Enviar para GitHub
git push
```

## ⚠️ Importante

1. **Nunca faça commit dos dados grandes!**
   - O .gitignore já protege contra isso
   - Se acidentalmente adicionar, use: `git rm --cached arquivo`

2. **Tamanho do repositório:**
   - Atual: ~500 KB (só código e docs)
   - Se incluir dados: ~700 MB (NÃO FAZER!)

3. **Privacidade:**
   - Remover informações sensíveis antes de fazer push
   - Verificar que não há credenciais no código

## 🔗 Clonar o Repositório em Outro Local

Depois de subir, qualquer pessoa pode clonar:

```bash
git clone https://github.com/SEU-USUARIO/proyecto-kdd-cup-1999.git
cd proyecto-kdd-cup-1999

# Baixar os dados (não estão no repo)
cd datos/raw
wget https://kdd.org/cupfiles/KDDCupData/1999/kddcup.data.zip
unzip kddcup.data.zip
mv kddcup.data.txt kddcup.data
```

## 📚 Recursos Úteis

- [GitHub Docs - Crear un repositorio](https://docs.github.com/es/repositories/creating-and-managing-repositories/creating-a-new-repository)
- [Git Basics](https://git-scm.com/book/en/v2/Getting-Started-Git-Basics)
- [GitHub CLI](https://cli.github.com/)

---

**Dúvidas?** Consulte a documentação do GitHub ou execute `git help <comando>`
