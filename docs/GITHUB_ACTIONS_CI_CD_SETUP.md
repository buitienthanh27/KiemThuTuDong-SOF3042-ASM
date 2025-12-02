# 🚀 GitHub Actions CI/CD Demo

## 📋 Quick Start

### Bước 1: Push code lên GitHub

```bash
git add .
git commit -m "Add CI/CD pipeline"
git push origin main
```

### Bước 2: Xem kết quả

1. Vào repository trên GitHub
2. Click tab **Actions**
3. Chọn workflow run để xem chi tiết

✅ **Xong!** Workflow tự động chạy khi có push/PR.

---

## 📁 Workflow File

### `github-actions-ci-cd-demo.yml` - Automation Test Pipeline

**Tính năng:**
- ✅ Setup MySQL database (vegana_store)
- ✅ Import database schema từ vegana.sql
- ✅ Start Spring Boot application
- ✅ Run TestNG automation tests (Login Tests & Register Tests)
- ✅ Upload test reports (Extent Reports, Screenshots, Logs)
- ✅ Upload application logs

**Test Suite:**
- Login Tests (`LoginTest.java`)
- Register Tests (`RegisterTest.java`)

**Trigger:**
- Push vào `main` hoặc `develop`
- Pull Request
- Manual trigger (workflow_dispatch)

---

## ⚙️ Configuration

### Database Settings

- **Host**: `localhost:3306`
- **Database**: `vegana_store`
- **Username**: `root`
- **Password**: `123456`

### Test Configuration

File: `src/test/resources/test.properties`
- **Base URL**: `http://localhost:9090`
- **Browser**: `chrome`
- **Test Suite**: `src/test/resources/testng.xml`

---

## 📊 View Results

### Test Reports

1. Vào **Actions** tab
2. Chọn workflow run
3. Scroll xuống **Artifacts**
4. Download `test-reports` để xem:
   - **Extent Reports**: HTML reports tại `test-output/reports/`
   - **Screenshots**: Screenshots khi test fail tại `test-output/screenshots/`
   - **Logs**: Test logs tại `test-output/logs/`

### Application Logs

Download `app-logs` artifact để xem Spring Boot application logs.

### Manual Trigger

1. Vào **Actions** tab
2. Chọn workflow **GitHub Actions CI/CD Demo**
3. Click **Run workflow**
4. Chọn branch và click **Run workflow**


## ✅ Checklist

- [ ] Workflow file đã có trong `.github/workflows/github-actions-ci-cd-demo.yml`
- [ ] Push code lên GitHub
- [ ] Workflow tự động chạy
- [ ] MySQL database setup thành công
- [ ] Application start thành công
- [ ] Tests chạy thành công
- [ ] Reports được upload

---
DEMO THÀNH CÔNG

**Happy Testing! 🚀**
