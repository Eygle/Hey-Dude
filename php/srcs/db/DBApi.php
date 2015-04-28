<?php
if (!defined('DBCONFIG_FILE')) {
    define('DBCONFIG_FILE', 'dbconfig.php');
}
require_once DBCONFIG_FILE;
require_once 'DAO.php';

define("TIMEOUT_ONLINE_USER", 120);
define("TIMEOUT_CALLS", 30);

class DBApi extends DAO
{

    function __construct($db = DB_DATABASE)
    {
        parent::__construct(null, $db);
    }

    public function login($gid, $name, $image, $email, $token, $pubk)
    {
        $stmt = $this->pdo->prepare("
        INSERT
          INTO users(gid, name, image, email, pubk, token)
          VALUES(:gid, :name, :image, :email, :pubk, :token)
          ON DUPLICATE KEY UPDATE token=VALUES(token), pubk=VALUES(pubk);
        INSERT
          INTO online_users(gid, timestamp)
          VALUES(:gid, NOW())
          ON DUPLICATE KEY UPDATE timestamp=VALUES(timestamp);
        ");

        $stmt->execute(array(
            ":gid" => $gid,
            ":name" => $name,
            ":image" => $image,
            ":email" => $email,
            ":token" => $token,
            ":pubk" => $pubk
        ));
    }

    public function logout($gid)
    {
        $stmt = $this->pdo->prepare("DELETE FROM online_users WHERE gid = :gid;");

        $stmt->execute(array(
            ":gid" => $gid
        ));
    }

    public function deleteAccount($gid)
    {
        $stmt = $this->pdo->prepare("DELETE FROM users WHERE gid = :gid;");

        $stmt->execute(array(
            ":gid" => $gid
        ));
    }

    public function onlineUsers($gid = null)
    {
        if ($gid) {
            // Remove user that timeout and refresh timestamp of current user
            $stmt = $this->pdo->prepare("
            INSERT
              INTO online_users(gid, timestamp)
              VALUES(:gid, NOW())
              ON DUPLICATE KEY UPDATE timestamp=VALUES(timestamp);
            DELETE
              FROM online_users
              WHERE TIMESTAMPDIFF(SECOND, timestamp, NOW()) >= " . TIMEOUT_ONLINE_USER . ";
            ");

            $stmt->execute(array(
                ":gid" => $gid
            ));
        }

        // Return all online users except yourself
        $stmt = $this->pdo->prepare("
        SELECT online_users.gid AS gId, users.image, users.name, users.email
          FROM online_users
          JOIN users
          ON users.gid = online_users.gid
        ");

        $stmt->execute();
        return array("users" => $stmt->fetchAll(PDO::FETCH_ASSOC));
    }

    public function getToken($gid) {
        $stmt = $this->pdo->prepare("SELECT token FROM users WHERE gid = :gid;");
        $stmt->execute(array(
            ":gid" => $gid,
        ));

        $res = $stmt->fetch(PDO::FETCH_ASSOC);
        return $res["token"];
    }

    public function getKey($gid) {
        $stmt = $this->pdo->prepare("SELECT pubk FROM users WHERE gid = :gid;");
        $stmt->execute(array(
            ":gid" => $gid,
        ));

        $res = $stmt->fetch(PDO::FETCH_ASSOC);
        return $res["pubk"];
    }

    public function getOnlineUsersTokens() {
        $stmt = $this->pdo->prepare("SELECT token FROM users WHERE gid IN (SELECT gid FROM online_users);");
        $stmt->execute();
        $res = $stmt->fetchAll(PDO::FETCH_ASSOC);

        $arr = array();
        foreach ($res as $v) {
            $arr[] = $v["token"];
        }
        return $arr;
    }
}