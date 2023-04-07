# pathes
LIBDIR = lib
LIB = $(LIBDIR)/*
# NB: may be done better, I haven't found a way to append these pathes
CLASSPATH = $(LIBDIR)/jfreechart.jar:$(LIBDIR)/jcommon.jar:$(LIBDIR)/JTattoo-1.6.11.jar
MANIFEST = Manifest
OUT = mmrs.jar

# rules
$(OUT): $(LIB) *.java
	javac -classpath $(CLASSPATH) *.java
	jar cfm $(OUT) $(MANIFEST) *.class

java:
	java -jar $(OUT)

clean:
	rm -f *{.class,~} $(OUT)
