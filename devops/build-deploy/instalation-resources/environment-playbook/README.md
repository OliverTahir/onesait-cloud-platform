# Prepare environment playbook

This playbook prepare VM to install onesait platform in three steps:

- **Step 1**: Create folders, certificates and gives permissions
- **Step 2**: Install Docker, configure insecure registries and change default docker dir (/var/lib/docker)
- **Step 3**: Copy docker-compose to start Rancher server

Pre requisites:

- First of it all install Ansible > 2.3
- Generate cert and copy to VM:
	- ssh-keygen -b 4096 -t rsa
	- scp .ssh/id_rsa.pub root@1.2.3.4:/~
	- cat /home/user/id_rsa.pub >> /home/user/.ssh/authorized_keys
	- chmod 644 /home/user/.ssh/authorized_keys
- Add vm ip to hosts file
- Copy hosts file to **/etc/ansible**
- Set the **root_directory** and the **fqdn_server_name** on **/group_vars/all**
- Set the VM user on **/site.yml remote_user** and his password on **ansible_become_pass**
- Finally run **ansible-playbook site.yml** or **ansible-playbook site.yml --check** (check flag only por debug purposes)

