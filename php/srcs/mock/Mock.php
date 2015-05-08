<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 04/03/2015
 * Time: 22:25
 */

/**
 * Class Mock: DEPRECATED
 */
class Mock {
    private $mockCallerFile;
    private $mockOnlineFile;
    private $mockStatusFile;
    private $mockKnownFile;

    public function __construct() {
        $dir = "mock/";

        $this->mockCallerFile = $dir."mock_caller";
        $this->mockOnlineFile = $dir."mock_online";
        $this->mockStatusFile = $dir."mock_status";
        $this->mockKnownFile = $dir."mock_known_users";
    }

    /**
     * Store user in online user file (simulate login)
     * @param $user
     * @param $file
     */
    private function addUserInFile($user, $file) {
        $users = json_decode(file_get_contents($file), true);

        foreach ($users as $u) {
            if ($u["gId"] == $user['gId']) return;
        }

        $users[] = $user;

        file_put_contents($file, json_encode(array_values($users)));
    }

    /**
     * Remove user from user online file (simulate logout)
     * @param $gId
     * @param $file
     */
    private function removeUserFromFile($gId, $file) {
        $users = json_decode(file_get_contents($file), true);

        foreach ($users as $k=>$u) {
            if ($u["gId"] == $gId) {
                unset($users[$k]);
            }
        }

        file_put_contents($file, json_encode(array_values($users)));
    }

    /**
     * Return online users list (from file)
     * @return array
     */
    public function onlineUsers() {
        $arr = array();

        $users = json_decode(file_get_contents($this->mockOnlineFile), true);

        $arr['users'] = $users;
        return $arr;
    }

    /**
     * Return user that are calling you
     * @return array
     */
    public function whoIsCallingMe() {
        $arr = array();

        $user = json_decode(file_get_contents($this->mockCallerFile), true);
        if ($user != null && is_array($user)) {
            $arr = $user;
            file_put_contents($this->mockCallerFile, "");
        }
        return $arr;
    }

    /**
     * Get call status
     * @return mixed
     */
    public function callStatus() {
        $arr = json_decode(file_get_contents($this->mockStatusFile), true);
        if ($arr["status"] == "accept") {
            $arr['key'] = "qfqmojdqmojd23ee2345654RFesf";
            file_put_contents($this->mockStatusFile, json_encode(array("status" => "")));
        } elseif($arr["status"] == "refuse") {
            file_put_contents($this->mockStatusFile, json_encode(array("status" => "")));
        } elseif ($arr["status"] == "wait" && time() - $arr['timestamp'] > 30) {
            file_put_contents($this->mockStatusFile, json_encode(array("status" => "")));
            $arr["status"] = "timeout";
        }

        return $arr;
    }

    /**
     * login user
     * @param $gId
     * @param $name
     * @param $image
     * @param $email
     */
    public function login($gId, $name, $image, $email) {
        $new = array( "gId" => $gId,
            "image" => $image,
            "name" => $name,
            "email" => $email,
            "IP" => mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255).".".mt_rand(0,255),
            "publicKey" => md5(time()));

        $this->addUserInFile($new, $this->mockOnlineFile);
        $this->addUserInFile($new, $this->mockKnownFile);
    }

    /**
     * Logout user
     * @param $gId
     */
    public function logout($gId) {
        $this->removeUserFromFile($gId, $this->mockOnlineFile);
    }

    /**
     * Call receiver
     * @param $gId
     */
    public function call($gId) {
        $users = json_decode(file_get_contents($this->mockOnlineFile), true);

        foreach ($users as $k=>$u) {
            if ($u["gId"] == $gId) {
                file_put_contents($this->mockCallerFile, json_encode($u));
            }
        }
        file_put_contents($this->mockStatusFile, json_encode(array("status" => "wait", "timestamp" => time())));
    }

    /**
     * Hangup call
     * @param $id
     */
    public function hangup($id) {
        // Not implemented
    }

    /**
     * Answer call
     * @param $status
     */
    public function answer($status) {
        file_put_contents($this->mockStatusFile, json_encode(array("status" => $status, "timestamp" => time())));
    }

    /**
     * Remove account from mock
     * @param $gId
     */
    public function deleteAccount($gId) {
        $this->logout($gId);
        $this->removeUserFromFile($gId, $this->mockKnownFile);
    }
}
