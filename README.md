# Universe Scout

Universe Scout is an NBA2K simulation universe data platform built with Spring Boot, Vue, MySQL, and vision model integration.

## Project Goals

1. Upload NBA2K simulation screenshots or zip image packages.
2. Parse screenshots into fixed JSON with a vision model.
3. Confirm parsed data manually before saving it.
4. Track team lineup changes.
5. Generate AI lineup analysis reports.

## Project Structure

- `src/main/java`: Spring Boot backend
- `universe-scout-web`: Vue3 frontend
- `docs`: project notes
- `sql`: database schema and seed scripts

## Tech Stack

Backend:

- Java 21
- Spring Boot 3
- MyBatis-Plus
- Sa-Token
- MySQL 8
- Hutool
- Knife4j

Frontend:

- Vue3
- Vite
- Axios
- Pinia
- Vue Router
- ECharts

## Local Run

Backend:

```powershell
mvn spring-boot:run
```

Frontend:

```powershell
cd universe-scout-web
npm.cmd install
npm.cmd run dev
```

Frontend URL:

```text
http://127.0.0.1:5173/
```

## Day 2 File APIs

- `POST /api/files/upload/images`: upload one or more images
- `POST /api/files/upload/zip`: upload a zip package
- `GET /api/files/batches`: page upload batches
- `GET /api/files/batches/{batchId}`: get batch detail and files

Local storage paths:

- Uploads: `D:/universe-scout/uploads`
- Extracted zip images: `D:/universe-scout/extracted`
