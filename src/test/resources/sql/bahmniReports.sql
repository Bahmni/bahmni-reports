 CREATE TABLE `scheduled_report` (
  `id` varchar(60) NOT NULL,
  `name` varchar(256) NOT NULL,
  `user` varchar(70) NOT NULL,
  `file_name` varchar(256) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `format` varchar(128) NOT NULL,
  `request_datetime` datetime NOT NULL,
  `error_message` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1