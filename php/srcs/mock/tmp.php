<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 06/03/2015
 * Time: 19:45
 */

$u = array();

$u["Bob"] = array(
    "gId" => 1234567890,
    "image" =>  "https://i.ytimg.com/vi/M99nzyiS830/hqdefault.jpg",
    "name" => "Bob",
    "email" => "bob@gmail.com",
    "IP" => mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255),
    "publicKey" => md5(time()));
$u["Alice"] = array(
    "gId" => 1234567899,
    "image" =>  "http://www.proprofs.com/quiz-school/upload/yuiupload/1458266109.jpg",
    "name" => "Alice",
    "email" => "alice@gmail.com",
    "IP" => mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255),
    "publicKey" => md5(time()));

file_put_contents("mock_known_users", json_encode($u));