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
}
