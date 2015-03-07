<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 04/03/2015
 * Time: 22:25
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

    private function addUserInFile($user, $file) {
        $users = json_decode(file_get_contents($file), true);

        foreach ($users as $u) {
            if ($u["gId"] == $user['gId']) return;
        }

        $users[] = $user;

        file_put_contents($file, json_encode(array_values($users)));
    }

    private function removeUserFromFile($gId, $file) {
        $users = json_decode(file_get_contents($file), true);

        foreach ($users as $k=>$u) {
            if ($u["gId"] == $gId) {
                unset($users[$k]);
            }
        }

        file_put_contents($file, json_encode(array_values($users)));
    }

    public function onlineUsers() {
        $arr = array();

        $users = json_decode(file_get_contents($this->mockOnlineFile), true);

        $arr['users'] = $users;
        return $arr;
    }

    public function whoIsCallingMe() {
        $arr = array();

        $user = json_decode(file_get_contents($this->mockCallerFile), true);
        if ($user != null && is_array($user)) {
            $arr = $user;
            file_put_contents($this->mockCallerFile, "");
        }
        return $arr;
    }

    public function callStatus() {
        $arr = array();

        $arr["status"] = file_get_contents($this->mockStatusFile);
        if ($arr["status"] == "accept") {
            $arr['key'] = "qfqmojdqmojd23ee2345654RFesf";
            file_put_contents($this->mockStatusFile, "");
        } elseif($arr["status"] == "refuse") {
            file_put_contents($this->mockStatusFile, "");
        }
        return $arr;
    }

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

    public function logout($gId) {
        $this->removeUserFromFile($gId, $this->mockOnlineFile);
    }

    public function call($gId) {
        $users = json_decode(file_get_contents($this->mockOnlineFile), true);

        foreach ($users as $k=>$u) {
            if ($u["gId"] == $gId) {
                file_put_contents($this->mockCallerFile, json_encode($u));
                file_put_contents($this->mockStatusFile, "wait");
            }
        }
    }

    public function hangup($id) {
        //TODO
    }

    public function answer($status) {
        file_put_contents($this->mockStatusFile, $status);
    }

    public function remove($gId) {
        $this->logout($gId);
        $this->removeUserFromFile($gId, $this->mockKnownFile);
    }
}
