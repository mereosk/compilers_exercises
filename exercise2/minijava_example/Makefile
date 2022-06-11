all: compile

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac ./my_visitors/*.java
	javac ./symbol_table/*.java
	javac ./v_table/*.java
	javac Main.java

clean:
	@printf "Cleaning everything up ..."; \
	rm -f *.class my_visitors/*.class symbol_table/*.class v_table/*.class; \
	rm -f JavaCharStream.java minijava-jtb.jj MiniJavaParser* ParseException.java Token*; \
	rm -rf visitor syntaxtree; \
	echo " Done!"

