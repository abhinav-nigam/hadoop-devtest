Exec { path => [ "/bin/", "/sbin/" , "/usr/bin/", "/usr/sbin/" ] }

exec { 'apt-get update':
  command => 'apt-get update',
  tries   => 3
}

package { "openjdk-7-jdk" :
	ensure => "installed",
    require => Exec['apt-get update']
}

package { "maven2" :
	ensure => "installed",
    require => Exec['apt-get update'],
}

$sysPackages = [ 'build-essential', 'git' ]
package { $sysPackages:
  ensure => "installed",
  require => Exec['apt-get update'],
}

include hadoop
