> * What is the smallest and the biggest instance type (in terms of
>   virtual CPUs and memory) that you can choose from when creating an
>   instance?

The instance can go from 1 vCPU & 0.5 GiB of RAM (t2.nano), all the way up to 448 vCPU and 6144 GiB 


> * How long did it take for the new instance to get into the _running_
>   state?

Depending on various factors, it can take from a few seconds all the way to 5 minutes.

> * Using the commands to explore the machine listed earlier, respond to
>   the following questions and explain how you came to the answer:
>
>   * What's the difference between time here in Switzerland and the time set on
>     the machine?

The date on the server is UTC based:
```sh
bitnami@ip-10-0-1-10:~$ date
Wed 13 Mar 2024 07:52:49 PM UTC
```
While the one in my local computer is CET based:

```sh
red@reds-framework~$ date
Wed Mar 13 08:53:36 PM CET 2024
```

As we are currently out of the Daylight Saving Time period, we only have a difference of one hour with UTC.

>  * What's the name of the hypervisor?

Depends on what we follow. The Linux client says it is running on KVM:

```sh
root@ip-10-0-1-10:/home/bitnami# dmesg | grep -i hypervisor
[    0.000000] Hypervisor detected: KVM
```

But the virtualization layer is called HVM (maybe hardware VM?) in the aws console.

>  * How much free space does the disk have?
```sh
root@ip-10-0-1-10:/home/bitnami# df -h
Filesystem       Size  Used Avail Use% Mounted on
udev             466M     0  466M   0% /dev
tmpfs             96M  384K   95M   1% /run
/dev/nvme0n1p1   9.7G  3.2G  6.0G  35% /
tmpfs            476M     0  476M   0% /dev/shm
tmpfs            5.0M     0  5.0M   0% /run/lock
/dev/nvme0n1p15  124M   11M  114M   9% /boot/efi
tmpfs             96M     0   96M   0% /run/user/1000
```

35%, or 6G available.

* Try to ping the instance ssh srv from your local machine. What do you see?
  Explain. Change the configuration to make it work. Ping the
  instance, record 5 round-trip times.

```sh
❯ ping 15.188.43.46
PING 15.188.43.46 (15.188.43.46) 56(84) bytes of data.
^C
--- 15.188.43.46 ping statistics ---
45 packets transmitted, 0 received, 100% packet loss, time 45044ms
```

As we see, the ICMP packets are dropped between my local machine and the SSH instance.

In order to fix that, we can edit the security group rule that applies to the SSH instance, to allow all ICMPv4 from my ISP provided IP Address.

I don't think I have the required permissions to do so.

> * Determine the IP address seen by the operating system in the EC2
>   instance by running the `ifconfig` command. What type of address
>   is it? Compare it to the address displayed by the ping command
>   earlier. How do you explain that you can successfully communicate
>   with the machine?

> [!NOTE]
> `ifconfig` is deprecated, and `ip <command>` is recommended instead.

```sh
root@ip-10-0-1-10:/home/bitnami# ip a
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens5: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 9001 qdisc mq state UP group default qlen 1000
    link/ether 06:1b:75:34:fc:1f brd ff:ff:ff:ff:ff:ff
    altname enp0s5
    inet 10.0.1.10/28 brd 10.0.1.15 scope global dynamic ens5
       valid_lft 2783sec preferred_lft 2783sec
    inet6 fe80::41b:75ff:fe34:fc1f/64 scope link 
       valid_lft forever preferred_lft forever
root@ip-10-0-1-10:/home/bitnami# ip r
default via 10.0.1.1 dev ens5 
10.0.1.0/28 dev ens5 proto kernel scope link src 10.0.1.10 
```

As we see, we have a private address in the subnetwork we configured.

Also, the default route is set to 10.0.1.1, an IP that is not addressed! The subnet default rules seems to redirect everything to the NAT machine, as wanted.