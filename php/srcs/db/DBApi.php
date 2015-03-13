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

    public function login($gid, $name, $image, $email, $pubk, $ip)
    {
        $stmt = $this->pdo->prepare("
        INSERT
          INTO users(gid, name, image, email, pubk, ip)
          VALUES(:gid, :name, :image, :email, :pubk, :ip)
          ON DUPLICATE KEY UPDATE ip=VALUES(ip);
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
            ":pubk" => $pubk,
            ":ip" => $ip
        ));
    }

    public function logout($gid)
    {
        $stmt = $this->pdo->prepare("DELETE FROM online_users WHERE gid = :gid;");

        $stmt->execute(array(
            ":gid" => $gid
        ));
    }

    public function call($gid, $destgid)
    {
        $stmt = $this->pdo->prepare("
        INSERT
          INTO calls(caller_gid, dest_gid, timestamp, status)
          VALUES(:gid, :destgid, NOW(), 'wait');
        ");

        $stmt->execute(array(
            ":gid" => $gid,
            ":destgid" => $destgid
        ));
    }

    public function hangup($gid, $destgid)
    {
        $stmt = $this->pdo->prepare("
        DELETE
          FROM calls
          WHERE caller_gid = :gid
          AND dest_gid = :destgid;
        INSERT
          INTO online_users(gid, timestamp)
          VALUES(:gid, NOW());
        INSERT
          INTO online_users(gid, timestamp)
          VALUES(:destgid, NOW());
        ");;

        $stmt->execute(array(
            ":gid" => $gid,
            ":destgid" => $destgid
        ));
    }

    public function answer($gid, $destgid, $status)
    {
        $stmt = $this->pdo->prepare("UPDATE calls
          SET status=:status
          WHERE caller_gid = :destgid
          AND dest_gid = :gid;");

        $stmt->execute(array(
            "gid" => $gid,
            "destgid" => $destgid,
            "status" => $status
        ));
    }

    public function deleteAccount($gid)
    {
        $stmt = $this->pdo->prepare("DELETE FROM users WHERE gid = :gid;");

        $stmt->execute(array(
            ":gid" => $gid
        ));
    }

    public function onlineUsers($gid)
    {
        // Remove user that timeout and refresh timestamp of current user
        $stmt = $this->pdo->prepare("
        INSERT
          INTO online_users(gid, timestamp)
          VALUES(:gid, NOW())
          ON DUPLICATE KEY UPDATE timestamp=VALUES(timestamp);
        DELETE
          FROM online_users
          WHERE TIMESTAMPDIFF(SECOND, timestamp, NOW()) >= ".TIMEOUT_ONLINE_USER.";
        ");

        $stmt->execute(array(
            ":gid" => $gid
        ));

        // Return all online users except yourself
        $stmt = $this->pdo->prepare("
        SELECT online_users.gid, users.image, users.name, users.email, users.ip
          FROM online_users
          JOIN users
          ON users.gid = online_users.gid
          WHERE users.gid != :gid;
        ");

        $stmt->execute(array(
            ":gid" => $gid
        ));
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function callStatus($gid, $destgid)
    {
        $this->pdo->exec("UPDATE calls
            SET status = 'timeout'
            WHERE TIMESTAMPDIFF(SECOND, timestamp, NOW()) >= ".TIMEOUT_CALLS);

        $stmt = $this->pdo->prepare("
        SELECT status
          FROM calls
          WHERE caller_gid = :gid
          AND dest_gid = :destgid;
        ");

        $stmt->execute(array(
            ":gid" => $gid,
            ":destgid" => $destgid
        ));

        $status = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($status['status'] == 'accept') {
            $stmt = $this->pdo->prepare("
            SELECT pubk
              FROM users
              WHERE gid = :destgid;
            ");

            $stmt->execute(array(
                ":destgid" => $destgid
            ));
            $pubk = $stmt->fetch(PDO::FETCH_ASSOC);
            $status["key"] = $pubk["pubk"];
        }
        if ($status["status"] != "wait") {
            $stmt = $this->pdo->prepare("DELETE FROM calls WHERE caller_gid = :gid AND dest_gid = :destgid");
            $stmt->execute(array(":gid" => $gid, ":destgid" => $destgid));
        }
        return $status;
    }

    public function whoIsCallingMe($gid)
    {
        $stmt = $this->pdo->prepare("
        SELECT users.gid, users.image, users.name, users.email, users.ip
          FROM calls
          JOIN users
          ON users.gid = calls.caller_gid
          WHERE caller_gid = :gid;
        ");

        $stmt->execute(array(
            ":gid" => $gid,
        ));

        return $stmt->fetch(PDO::FETCH_ASSOC);
    }
}