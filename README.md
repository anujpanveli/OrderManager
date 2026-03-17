Requirements:

1) JDK 21
2) Camunda 8


Running Instructions:

Before running the backend, camunda must be running on local instance, so backend can interact using http://localhost:8080

1) Option A — Using Docker (recommended):
    docker-compose up --build

2) Option B — Manual steps without Docker:
      Step 1: mvn clean compile test package
      Step 2: java -jar target\OrderManager-0.0.1-SNAPSHOT.jar

3) Option C — Manual steps without Docker: 
      Step 1: Directly run the java file Application.java inside src/main/java/com/example/OrderManager
