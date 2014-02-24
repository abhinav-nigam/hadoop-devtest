Vagrant.configure("2") do |config|
  config.vm.box = "precise-64"
  config.vm.box_url = "http://cloud-images.ubuntu.com/vagrant/precise/current/precise-server-cloudimg-amd64-vagrant-disk1.box"

  config.vm.synced_folder  "./", "/vagrant", id: "vagrant-root"
  config.vm.provider "virtualbox" do |v|
     v.customize ["modifyvm", :id, "--memory", 4096]
     v.customize ["modifyvm", :id, "--cpus", 4]
	 v.customize ["modifyvm", :id ,"--cpuexecutioncap", "50"]
  end


  config.vm.provision :puppet do |puppet|
  	puppet.manifests_path = "puppet/manifests"
	puppet.options = ['--verbose']
	puppet.module_path = "puppet/modules"
 end
end
