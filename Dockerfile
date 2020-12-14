FROM openjdk:12
WORKDIR /app/
COPY src ./src
RUN javac -sourcepath src src/jcompiler/JCompiler.java -d .