# 🎓 Elective Navigator

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

An intelligent, dual-module system designed to streamline the process of discovering, managing, and selecting academic electives. 

The project strictly links a **Desktop Admin Panel** with a **Responsive Web App** for students, all synchronized instantaneously using MongoDB Atlas. Students are exclusively presented with the exact subject choices relevant to their active semester and knowledge criteria.

---

## ✨ Features

### 👨‍💻 Admin Module (Desktop App)
- **Built with Java Swing**: Fast, native desktop software for university administrators.
- **Elective Management**: Admins can securely add, edit, or delete available electives.
- **Direct Cloud Sync**: Instantly pushes data (Subject Name, Semester, Teacher) to the MongoDB Atlas cluster.

### 🧑‍🎓 Student Module (Web App)
- **Modern Premium UI**: Built with Tailwind CSS, featuring glassmorphism elements, dark mode aesthetics, and clean typography.
- **Authentication**: Secure student registration and login flows.
- **Dynamic Semantic Fetching**: The dashboard intelligently identifies the student's current semester and isolates the electives fetched from the Admin's database.
- **Deploy Ready**: Fully consolidated (Servlets + Static HTML/JS serving via Tomcat) and native Dockerfile support for immediate cloud hosting.

---

## 🛠️ Tech Stack 

- **Frontend**: HTML5, Vanilla JavaScript, Tailwind CSS (via CDN)
- **Backend (Web)**: Java 17, Java Servlets (Jarkata API), Maven, Apache Tomcat
- **Backend (Desktop)**: Java SWING (AWT/Swing Toolkit)
- **Database**: MongoDB Atlas (Cloud NoSQL DB)
- **Deployment & Ops**: Docker (Multi-stage builds), Render 

---

## 📁 Project Structure

```text
📦 Elective Navigator
 ┣ 📂 admin/                     # Java Swing PC Application for Administrators
 ┃ ┣ 📜 AddElective.java
 ┃ ┣ 📜 AdminDashboard.java
 ┃ ┗ 📜 MongoDBConnection.java   
 ┣ 📂 lib/                       # JAR Dependencies used by the Admin module
 ┗ 📂 student/
   ┗ 📂 backend/                 # Student Web App (Java Servlets + Frontend UI)
     ┣ 📂 src/main/java/         # Servlet API Logic (Auth, Fetch Electives)
     ┣ 📂 src/main/webapp/       # HTML, TailwindCSS, and app.js Frontend files
     ┣ 📜 Dockerfile             # Multi-stage Docker config for Render/Cloud Deploy
     ┣ 📜 render.yaml            # Render Infrastructure-as-code configuration
     ┗ 📜 pom.xml                # Maven Dependencies
```

---

## 🚀 How to Run Locally

### 1. Database Setup
- Ensure you have a MongoDB Atlas connection. 
- Replace the `URI` constants inside both `admin/MongoDBConnection.java` and `student/backend/src/main/java/.../MongoDBConnection.java` if needed.

### 2. Running The Admin Software
- Navigate to the `admin/` directory.
- Compile and run `AdminLogin.java` via your Java IDE (Eclipse, VSCode, IntelliJ) with the `.jar` files in `lib/` included in the class path.
- Add some electives for "4th Semester" to test it out!

### 3. Running The Student Web App
- Navigate to `student/backend/` and open it as a **Maven Project**.
- Start a Tomcat (v9+) server locally running on port `8080` or use `mvn tomcat7:run`.
- Open your browser to `http://localhost:8080/`. You will be greeted by the stunning landing page where you can register and observe the live database sync.

---

## ☁️ Deployment on Render

This project is meticulously pre-configured to be deployed natively on [Render](https://render.com/) utilizing Docker. 

1. Push this whole folder to a free **GitHub Repository**.
2. Log into the Render Dashboard and click **New > Web Service**.
3. Point to your connected Github repository.
4. Render will seamlessly detect the `Dockerfile` in the `student/backend` folder, auto-build your Java Maven container, and deploy an operational Tomcat server.
5. Profit! Your Live Elective Navigator is now active.
