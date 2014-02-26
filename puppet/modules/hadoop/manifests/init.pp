class hadoop {
		$hadoop_home = "/opt/hadoop"
		exec { "get_hadoop":
				command => "wget -O /tmp/hadoop.tar.gz http://mirrors.ibiblio.org/apache/hadoop/common/hadoop-1.2.1/hadoop-1.2.1-bin.tar.gz", 
				path => $path,
				unless => "ls /opt | grep hadoop-1.2.1",
				require => Package["openjdk-7-jdk"]
		}

       exec { "install_hadoop" :
			   command => "tar -zxf /tmp/hadoop.tar.gz -C /opt",
			   path => $path,
			   creates => "${hadoop_home}-1.2.1",
			   require => Exec["get_hadoop"]
		  }
file {
  "${hadoop_home}-1.2.1/conf/slaves":
  source => "puppet:///modules/hadoop/slaves",
  mode => 644,
  owner => root,
  group => root,
  require => Exec["install_hadoop"]
 }
 
file {
  "${hadoop_home}-1.2.1/conf/masters":
  source => "puppet:///modules/hadoop/masters",
  mode => 644,
  owner => root,
  group => root,
  require => Exec["install_hadoop"]
 }

file {
  "${hadoop_home}-1.2.1/conf/core-site.xml":
  source => "puppet:///modules/hadoop/core-site.xml",
  mode => 644,
  owner => root,
  group => root,
  require => Exec["install_hadoop"]
 }
 
file {
  "${hadoop_home}-1.2.1/conf/mapred-site.xml":
  source => "puppet:///modules/hadoop/mapred-site.xml",
  mode => 644,
  owner => root,
  group => root,
  require => Exec["install_hadoop"]
 }
 
 file {
  "${hadoop_home}-1.2.1/conf/hdfs-site.xml":
  source => "puppet:///modules/hadoop/hdfs-site.xml",
  mode => 644,
  owner => root,
  group => root,
  require => Exec["install_hadoop"]
 }
file {
  "/root/.ssh/id_rsa":
  source => "puppet:///modules/hadoop/id_rsa",
  mode => 600,
  owner => root,
  group => root,
  require => Exec['apt-get update']
 }
 
file {
  "/root/.ssh/id_rsa.pub":
  source => "puppet:///modules/hadoop/id_rsa.pub",
  mode => 644,
  owner => root,
  group => root,
  require => Exec['apt-get update']
 }

ssh_authorized_key { "ssh_key":
    ensure => "present",
    key    => "AAAAB3NzaC1yc2EAAAADAQABAAABAQDN3S7nKi4MhCwrFjuHcxu08IQMug0lb890xsoi8LxRugPJl1d8pi/ZZfcd8em2KNH2R2RYfiUqbYZYe3BnU+NtMwdYi9+Tf2Kh4yick0WnraeiV9BPfgKhU2JcpmjPqUBYBfmsVHH9q54QixUopCmZ+r+z+M7Ct4ZByFoHOPuiVDHUwRjBeHwTnhF60aTONiGtkPsTqyXWoJvo5zdv3EJ/duaHJ/kr/RhIu7HTP3aGV0PwJSSqwlFpWDDKH3QVnuQW23Ds1GYNDil73UNTh66uy9sf+n9V2XahyZogy9IfszGyvC+sutplVyRigCPz5ad/STWWnPOjhzSNP09DBr8p",
    type   => "ssh-rsa",
    user   => "root",
    require => File['/root/.ssh/id_rsa.pub']
}

file {
 "${hadoop_home}-1.2.1/conf/hadoop-env.sh":
 source => "puppet:///modules/hadoop/hadoop-env.sh.tpl",
 mode => 644,
 owner => root,
 group => root,
 require => Exec["install_hadoop"]
}
}
