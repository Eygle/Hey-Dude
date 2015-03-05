<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 04/03/2015
 * Time: 22:25
 */

require_once("utils/Utils.php");

class Mock {
    private $active;
    private $users;

    public function __construct($mock = false) {
        $this->active = $mock;

        $this->mockCallerFile = "mock_caller";

        $this->users["Bob"] = array(
            "gId" => 1234567890,
            "gImage" =>  "https://i.ytimg.com/vi/M99nzyiS830/hqdefault.jpg",
            "name" => "Bob",
            "email" => "bob@gmail.com",
            "IP" => "22.96.54.32");
        $this->users["Alice"] = array(
            "gId" => 1234567899,
            "gImage" =>  "http://www.proprofs.com/quiz-school/upload/yuiupload/1458266109.jpg",
            "name" => "Alice",
            "email" => "alice@gmail.com",
            "IP" => "22.96.54.32");

        $this->checkMockActions();
    }

    private function checkMockActions() {
        if (isset($_GET['action'])) {
            switch($_GET['action']) {
                case 'mock_set_caller':
                    Utils::checkParams($_GET, "name", array("Bob", "Alice"));

                    file_put_contents($this->mockCallerFile, json_encode($this->users[$_GET['name']]));

                    echo $_GET['name']." was added as caller.";
                    exit(0);
                    break;
            }
        }
    }

    public function addOnlineUsers() {
        $arr = array();
        if (!$this->active) return $arr;

        $users = array();
        $users[] = $this->users["Bob"];
        $users[] = $this->users["Alice"];

        $arr['users'] = $users;
        return $arr;
    }

    public function addWhoIsCallingMe() {
        $arr = array();
        if (!$this->active) return $arr;

        $user = json_decode(file_get_contents($this->mockCallerFile), true);
        if ($user != null && is_array($user)) {
            $arr = $user;
            file_put_contents($this->mockCallerFile, "");
        }
        return $arr;
    }
}