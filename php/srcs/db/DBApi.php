<?php
if (!defined('DBCONFIG_FILE')) {
    define('DBCONFIG_FILE', 'dbconfig.php');
}
require_once DBCONFIG_FILE;
require_once 'DAO.php';

class DBApi extends DAO
{

    function __construct($db = DB_DATABASE)
    {
        parent::__construct(null, $db);
    }

    private function getTest()
    {
        $stmt = $this->pdo->prepare("SELECT * FROM users");
        $stmt->execute();
        $res1 = $stmt->fetch(PDO::FETCH_ASSOC); // Fetch only one line from db
        $res2 = $stmt->fetchAll(PDO::FETCH_ASSOC); // Fetch all occurences as a array
        return $res1;
    }
}