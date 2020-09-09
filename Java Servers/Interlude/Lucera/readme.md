# DenArt Designs Auto Donate Panel
Auto Donate Panel from DenArt Designs installed on https://iTopZ.com

### Lucera installation

You need to place the ```donate.ext.jar``` file inside your libs folder

In case you have 2 databases (login, game) you must install the SQL into game Database, otherwise it won't work.
first you need to create a new user/password for more security give him the following permissions

```
INSERT on donate_holder
UPDATE on donate_holder
```

The SQL Table:

```
CREATE TABLE `donate_holder` (
  `no` int(11) NOT NULL AUTO_INCREMENT,
  `id` int(11) NOT NULL,
  `count` int(11) NOT NULL,
  `playername` varchar(255) CHARACTER SET utf8 NOT NULL,
  `order_status` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;
```

You can use for FREE this version of the panel
More info on how to install Donate Panel for your server here: https://itopz.com/forum/main/donate-panel/24-how-to.html