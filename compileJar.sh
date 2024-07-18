javac -d ./build src/com/thefattestcat/GameOfLife/Main.java -cp src/
cd build/
jar -cfe ../gameOfLife.jar com.thefattestcat.GameOfLife.Main com/thefattestcat/GameOfLife/*.class