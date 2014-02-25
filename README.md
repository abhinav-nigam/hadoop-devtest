hadoop-devtest
==============

Setup Instructions (tested on any system which support vagrant):


0) copy your id_rsa and id_rsa.pub to puppet/modules/hadoop/files

1) vagrant up

1.5) vagrant ssh master , vagrant ssh hadoop1 .....

2) ssh from 10.10.10.* -> 10.10.10.* to make sure there are no prompts when hadoop attempts to ssh for the first time.

3) become root : sudo su

4) cd /opt/hadoop-1.2.1/bin && ./hadoop namenode -format 

5) ./start-all.sh

6) cd /vagrant/devtest [Assuming you did checkout this code, it should be available in a shared vagrant folder]

7) mvn clean test. 

If you run into issues with asm-3.1 jar, download and chepo it in the repo folder removing the previous text file maven thinks is a jar. 
