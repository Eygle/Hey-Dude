<?php

require_once("utils/FilesManager.php");
require_once("utils/Utils.php");

try {
    if ($_SERVER["REQUEST_METHOD"] == 'GET') {
        // Chech if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_GET, "action", array(array("get_online_users", "connection_status", "who_is_calling_me")));

        switch ($_GET['action']) {
            case "get_online_users":
                // TODO get all online users and print it

                // TODO delete following lines
                $users = array();
                $users[] = array(
                    "gId" => 1234567890,
                    "gImage" =>  "https://i.ytimg.com/vi/M99nzyiS830/hqdefault.jpg",
                    "login" => "Bob",
                    "email" => "bob@gmail.com",
                    "IP" => "22.96.54.32");
                $users[] = array("gId" => 1234567899,
                    "gImage" =>  "http://www.proprofs.com/quiz-school/upload/yuiupload/1458266109.jpg",
                    "login" => "Alice",
                    "email" => "alice@gmail.com",
                    "IP" => "22.96.54.32");
                echo json_encode(array("users" => $users));
                break;
            case "connection_status":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("myGId", "userGId"));

                // TODO
                break;
            case "who_is_calling_me":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId"));

                // TODO
                break;
        }
    } else if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Chech if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_POST, "action", array(array("add_user", "login", "connect_to_user", "close_connection", "answer")));

        switch ($_POST['action']) {
            case "add_user":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "login", "gImage", "publicKey"));

                // TODO
                break;
            case "login":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "login"));

                // TODO
                break;
            case "connect_to_user":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("myGId", "userGId", "myIP"));

                // TODO
                break;
            case "close_connection":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("myGId", "userGId"));

                // TODO
                break;
            case "answer":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("status", "myGId", "userGId"), array(array("accept", "refuse")));

                // TODO
                break;
        }
    } else {
        throw new Exception("Bad request.");
    }
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array("Error" => $e->getMessage()));
}