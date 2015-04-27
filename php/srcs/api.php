<?php

require_once("utils/Utils.php");
require_once("mock/Mock.php");
require_once("db/DBApi.php");

try {
    $display = array();
    $db = new DBApi();

    // Set the mock
    $mock = new Mock();

    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Check if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_POST, "action", array(array("login", "logout", "call", "hang_up", "answer", "delete_account", "sendMessage", "sendKey")));

        // Set the mock
        $mock = new Mock();
        $mockModeOn = isset($_POST['mock']) ? $_POST['mock'] : false;

        switch ($_POST['action']) {
            case "login":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "name", "image", "email", "token", "publicKey"));

                if (!$mockModeOn) {
                    $db->login($_POST["gId"], $_POST["name"], $_POST["image"], $_POST["email"], $_POST['token'], $_POST["publicKey"]);
                } else {
                    $mock->login($_POST["gId"], $_POST["name"], $_POST["image"], $_POST["email"]);
                }

                Utils::sendOnlineUsersList($db, $_POST['gId']);
                break;
            case "logout":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId"));

                if (!$mockModeOn) {
                    $db->logout($_POST["gId"]);
                } else {
                    $mock->logout($_POST["gId"]);
                }

                Utils::sendOnlineUsersList($db);
                break;
            case "call":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                if (!$mockModeOn) {
                    Utils::sendPush(array($db->getToken($_POST['destGId'])), array("action" => "call", "dest" => $_POST['gId']));
                } else {
                    $mock->call($_POST["gId"]);
                }
                break;
            case "hang_up":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                if (!$mockModeOn) {
                    Utils::sendPush(array($db->getToken($_POST['destGId'])), array("action" => "hangup", "dest" => $_POST['gId']));
                } else {
                    $mock->hangup($_POST["gId"]);
                }
                break;
            case "answer":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("status", "gId", "destGId"), array(array("accept", "refuse")));

                if (!$mockModeOn) {
                    if ($_POST["answer"]) {
                        Utils::sendPush(array($db->getToken($_POST['destGId'])), array("action" => "answer", "dest" => $_POST['gId'], "status" => $_POST["status"], "key" => $db->getKey($_POST['gId'])));
                    } else {
                        Utils::sendPush(array($db->getToken($_POST['destGId'])), array("action" => "answer", "dest" => $_POST['gId'], "status" => $_POST["status"]));
                    }
                } else {
                    $mock->answer($_POST['status']);
                }
                break;
            case "delete_account":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId"));

                if (!$mockModeOn) {
                    $db->deleteAccount($_POST['gId']);
                } else {
                    $mock->deleteAccount($_POST['gId']);
                }
                break;
            case "sendMessage":
                Utils::checkParams($_POST, array("destGId", "message", "iv"));

                $token = array($db->getToken($_POST['destGId']));
                $msg = array("action"=>"send_msg", "message" => $_POST['message'], "iv" => $_POST["iv"]);

                Utils::sendPush($token, $msg);
                break;
            case "sendKey":
                Utils::checkParams($_POST, array("destGId", "key"));

                $token = array($db->getToken($_POST['destGId']));
                $msg = array("action"=>"send_key", "key" => $_POST['key']);

                Utils::sendPush($token, $msg);
                break;
        }
    } else {
        throw new Exception("Bad request.");
    }
    echo empty($display) ? "{}" : json_encode($display);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(array("Error" => $e->getMessage()));
}