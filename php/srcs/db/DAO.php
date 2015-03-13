<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 06/03/2015
 * Time: 16:37
 */

class DAO{
    protected $pdo;

    function __construct(){
        $pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
        $pdo_options[PDO::MYSQL_ATTR_INIT_COMMAND] = "SET NAMES utf8";
        try{
            $this->pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_DATABASE, DB_USER, DB_PASSWORD, $pdo_options);
        } catch (PDOException $e) {
            print $e->getMessage();
            print "Erreur de connexion<br/>";
        }
    }
}
