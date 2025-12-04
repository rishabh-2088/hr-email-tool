# HR Email Response Tool

A simple web application that allows HR to preview and send email responses to job candidates using predefined templates (Selected / Rejected).

This project uses:
- **Frontend:** HTML, CSS (Tailwind), JavaScript
- **Backend:** Java + Spring Boot
- **Email:** SMTP (Mailtrap recommended for testing)

---

## ✨ Features
- Preview email before sending
- Templates with placeholders (name, position, company)
- Edit the generated email body before sending
- Send emails using SMTP
- Basic form validation (client + server)
- Clear success/error 
- No database required

---

## 📁 Project Structure

```
src/main/
├── java/com.rishabh.hrtool
│ ├── controller
│ ├── model
│ ├── service
│ ├── util
│ └── HrEmailToolApplication.java
└── resources
├── static/index.html
├── templates/selection.txt
├── templates/rejection.txt
└── application.properties
```

---

## 🚀 How to Run the Application
### 1. Build the project
```bash
mvn clean package
```

### 2. Run the Spring Boot app
```bash
mvn spring-boot:run
```

Or use IntelliJ → Run HrEmailToolApplication.

### 3. Open the application
Open in your browser:

```
http://localhost:8080
```



You will see the HR email form (Name, Email, Position, Status).

📧 Email Configuration (SMTP)

The app uses environment variables to avoid storing credentials in code.

For Mailtrap (recommended for testing):

In IntelliJ:

SPRING_MAIL_HOST=sandbox.smtp.mailtrap.io
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=<your mailtrap username>
SPRING_MAIL_PASSWORD=<your mailtrap password>


Spring Boot reads these automatically through:

spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}

Default "from" email:

Defined inside application.properties:

app.mail.from=hr@mycompany.com


Can be overridden in the UI.

🧪 Testing
1. Preview email

Click Preview → View subject/body → Edit if needed.

2. Send email

Click Send → Email will arrive in Mailtrap inbox.

3. Validation check

Leave fields empty → Server returns JSON validation errors.

📄 Templates

Located in:

src/main/resources/templates/
### selection.txt
```
Dear {{name}},

We are pleased to inform you that you have been selected for the position of {{position}} at {{company}}.

Best regards,
HR Team
```

### rejection.txt
```
Dear {{name}},

Thank you for applying for the position of {{position}} at {{company}}.
We regret to inform you that we have decided to move forward with other candidates.

Best regards,
HR Team
```

Placeholders automatically replaced:
```
{{name}}

{{position}}

{{company}}
```
📝 Notes

No database used — templates are files + simple string replace.

Secure credentials using environment variables.

Designed to be simple and easy to review.




---

