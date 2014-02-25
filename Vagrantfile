Vagrant.configure("2") do |config|
  config.vm.box = "precise-64"
  config.vm.box_url = "http://cloud-images.ubuntu.com/vagrant/precise/current/precise-server-cloudimg-amd64-vagrant-disk1.box"

  config.vm.synced_folder  "./", "/vagrant", id: "vagrant-root"
  config.vm.provider "virtualbox" do |v|
     v.customize ["modifyvm", :id, "--memory", 1024]
     v.customize ["modifyvm", :id, "--cpus", 4]
	 v.customize ["modifyvm", :id ,"--cpuexecutioncap", "50"]
  end


  config.vm.provision :puppet do |puppet|
  	puppet.manifests_path = "puppet/manifests"
	puppet.options = ['--verbose']
	puppet.module_path = "puppet/modules"
  end

  config.vm.define :master do |master|
     master.vm.network :private_network, ip: "10.10.10.1"
  end

  config.vm.define :backup do |backup|
    backup.vm.network :private_network, ip: "10.10.10.2"
  end
 
  config.vm.define :hadoop1 do |hadoop1|
    hadoop1.vm.network :private_network, ip: "10.10.10.3"
  end
 
  config.vm.define :hadoop2 do |hadoop2|
    hadoop2.vm.network :private_network, ip: "10.10.10.4"
  end
 
  config.vm.define :hadoop3 do |hadoop3|
    hadoop3.vm.network :private_network, ip: "10.10.10.5"
  end

end
