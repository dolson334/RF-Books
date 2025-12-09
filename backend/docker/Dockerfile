# Stage 1: Build the application
FROM gradle:7.6-jdk17-alpine AS build

WORKDIR /app

# 1) Copy only files that affect dependency resolution
# Adjust names if you use *.kts
COPY settings.gradle* build.gradle* gradle.properties ./

# 2) Download dependencies (runs only when build config changes)
RUN gradle --no-daemon dependencies || true

# 3) Now copy the rest of the source
COPY . .

# ... existing code ...
# 4) Actual build – reruns only when sources change
RUN gradle bootJar -x test --no-daemon --build-cache && \
    rm -f build/libs/*-plain.jar

# Stage 2: Extract Layers (New Stage)
# This extracts the fat jar into separate folders for dependencies, loader, and app code
FROM amazoncorretto:17-alpine as extractor
WORKDIR /app
COPY --from=build /app/build/libs/*.jar backend.jar
RUN java -Djarmode=layertools -jar backend.jar extract

# Stage 3: Run the application (Optimized)
FROM amazoncorretto:17-alpine

WORKDIR /app/backend

# Copy layers specifically (Order matters for caching)
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

EXPOSE 8080

# Use the JarLauncher instead of java -jar
# Optional: -XX:TieredStopAtLevel=1 speeds up startup significantly but reduces peak performance slightly
CMD ["java", "-XX:TieredStopAtLevel=1", "org.springframework.boot.loader.launch.JarLauncher"]