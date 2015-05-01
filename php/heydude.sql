-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: May 02, 2015 at 12:54 AM
-- Server version: 5.5.40-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `heydude`
--

-- --------------------------------------------------------

--
-- Table structure for table `calls`
--

CREATE TABLE IF NOT EXISTS `calls` (
  `caller_gid` varchar(50) NOT NULL,
  `dest_gid` varchar(50) NOT NULL,
  `timestamp` datetime NOT NULL,
  `status` varchar(50) NOT NULL,
  UNIQUE KEY `caller_gid` (`caller_gid`),
  UNIQUE KEY `dest_gid` (`dest_gid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `online_users`
--

CREATE TABLE IF NOT EXISTS `online_users` (
  `gid` varchar(50) NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`gid`),
  UNIQUE KEY `gid` (`gid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `gid` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `image` varchar(200) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `pubk` varchar(512) NOT NULL,
  `ip` varchar(15) NOT NULL,
  `port` int(11) NOT NULL,
  `token` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`gid`),
  UNIQUE KEY `pubk` (`pubk`),
  UNIQUE KEY `gid` (`gid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `calls`
--
ALTER TABLE `calls`
  ADD CONSTRAINT `caller_gid_key` FOREIGN KEY (`caller_gid`) REFERENCES `users` (`gid`) ON DELETE CASCADE ON UPDATE NO ACTION,
  ADD CONSTRAINT `dest_gid_key` FOREIGN KEY (`dest_gid`) REFERENCES `users` (`gid`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints for table `online_users`
--
ALTER TABLE `online_users`
  ADD CONSTRAINT `online_gid_key` FOREIGN KEY (`gid`) REFERENCES `users` (`gid`) ON DELETE CASCADE ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
