FROM openjdk:12
WORKDIR /app/
COPY src ./src
RUN javac -sourcepath src src/jcompiler/JCompiler.java -d .
COPY operands.txt ./jcomplier/operands.txt
COPY binary_operands.txt ./jcomplier/binary_operands.txt
COPY operand_priority.md ./jcomplier/operand_priority.md
COPY reserved.txt ./jcompiler/reserved.txt