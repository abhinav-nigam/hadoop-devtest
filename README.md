hadoop-devtest
==============

Setup Instructions (tested on Ubuntu 12.04):

1) Install Cloudera's Hadoop dist3 version 0.20, the steps to do that are as follows. Create a `/etc/apt/sources.list.d/cloudera.list` with the following contents:

`deb http://archive.cloudera.com/debian REL-cdh3 contrib`
`deb-src http://archive.cloudera.com/debian REL-cdh3 contrib`

Replace REL by the name of your distribution’s release, found by running:

`lsb_release -c`

Add the Cloudera signing key:

`curl -s http://archive.cloudera.com/debian/archive.key | sudo apt-key add -`

Then install Hadoop:

`apt-get install hadoop-0.20 hadoop-0.20-conf-pseudo`

To automatically launch Hadoop after reboot, also install the following packages:

`apt-get install hadoop-0.20-namenode hadoop-0.20-jobtracker hadoop-0.20-secondarynamenode hadoop-0.20-datanode hadoop-0.20-tasktracker`

`sudo su hdfs`
`hadoop namenode -format`

You can check Hadoop’s health in a web-based panel available at:

`http://localhost:50070/dfshealth.jsp`
`http://localhost:50030/jobtracker.jsp`


2) Clone the project. It has a folder named devtest, which is a maven project, and can be built from the commandline using familiar maven commands like

`mvn clean package`

or imported to eclipse, and the junit tests can be simply run using the run directive.
