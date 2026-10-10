# Data Science Digital Library

Two projects live here:

| Folder | What it is |
|---|---|
| [`ds-library/`](ds-library/README.md) | The portal: Spring Boot + MongoDB + the website. **Start here.** |
| [`code-runner/`](code-runner/README.md) | Optional separate service that runs Java, C and C++ in locked-down Docker containers. |

Quick start (needs JDK 17+, Maven and MongoDB):

```powershell
$env:ADMIN_PASSWORD = "choose-a-long-password"
cd ds-library
mvn spring-boot:run
```

Then open http://localhost:8085. Deployment steps are in [`ds-library/DEPLOYMENT.md`](ds-library/DEPLOYMENT.md).
