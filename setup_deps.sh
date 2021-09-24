#!/bin/bash

rm pom.xml

cat > pom.xml << EOF
<?xml version="1.0"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>mygroupid</groupId>
  <artifactId>myartifactid</artifactId>
  <version>0.0-SNAPSHOT</version>
  <dependencies>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>de.qaware.maven</groupId>
        <artifactId>go-offline-maven-plugin</artifactId>
        <version>1.2.5</version>
        <configuration>
          <dynamicDependencies>
            <DynamicDependency>
              <groupId>org.apache.maven.surefire</groupId>
              <artifactId>surefire-junit4</artifactId>
              <version>2.20.1</version>
              <classifier/>
              <repositoryType>PLUGIN</repositoryType>
            </DynamicDependency>
            <DynamicDependency>
              <groupId>com.querydsl</groupId>
              <artifactId>querydsl-apt</artifactId>
              <version>4.2.1</version>
              <classifier>jpa</classifier>
              <repositoryType>MAIN</repositoryType>
            </DynamicDependency>
          </dynamicDependencies>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>

EOF


# Clean things up (If you don't want to create a new file)
#./dependencies.py clean

# Dependencies from Maven
./dependencies.py add -a twilio
./dependencies.py add -a unirest-java -g com.konghq -v 3.12.0
./dependencies.py add -a junit -v 4.13.2
./dependencies.py add -a json -g org.json

# Prettify this
cat pom.xml | xmllint --format - > output.xml
mv output.xml pom.xml