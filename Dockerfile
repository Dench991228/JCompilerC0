FROM openjdk:12
WORKDIR /app/
COPY src ./src
RUN javac -sourcepath src src/jcompiler/JCompiler.java -d .
COPY operands.txt ./jcomplier
COPY binary_operands.txt ./jcomplier
COPY operand_priority.md ./jcomplier
COPY reserved.txt ./jcompiler