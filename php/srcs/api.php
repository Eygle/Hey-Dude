<?php

require_once("utils/Utils.php");
require_once("mock/Mock.php");
require_once("db/DBApi.php");

try {
    $display = array();
    $db = new DBApi();

    // Set the mock
    $mock = new Mock();

//    $_GET['mock'] = false;
//    $_POST['mock'] = false;
    if ($_SERVER["REQUEST_METHOD"] == 'GET') {

        // Check if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_GET, "action", array(array("online_users", "call_status", "who_is_calling_me")));

        switch ($_GET['action']) {
            case "online_users":
                Utils::checkParams($_GET, array("gId"));

                if (!$_GET['mock']) {
                    $display = $db->onlineUsers($_GET["gId"]);
                } else {
                    $display = $mock->onlineUsers();
                }
                break;
            case "call_status":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId", "destGId"));

                if (!$_GET['mock']) {
                    $display = $db->callStatus($_GET["gId"], $_GET["destGId"]);
                } else {
                    $display = $mock->callStatus();
                }
                break;
            case "who_is_calling_me":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId"));

                if (!$_GET['mock']) {
                    $display = $db->whoIsCallingMe($_GET["gId"]);
                } else {
                    $display = $mock->whoIsCallingMe();
                }
                break;
        }
    } else if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Check if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_POST, "action", array(array("login", "logout", "call", "hang_up", "answer", "delete_account")));

        // Set the mock
        $mock = new Mock(isset($_POST['mock']) && $_POST['mock'] == "true");

        switch ($_POST['action']) {
            case "login":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "name", "image", "email", "publicKey"));

                if (!$_POST['mock']) {
                    $db->login($_POST["gId"], $_POST["name"], $_POST["image"], $_POST["email"], $_POST["publicKey"], $_SERVER["REMOTE_ADDR"]);
                } else {
                    $mock->login($_POST["gId"], $_POST["name"], $_POST["image"], $_POST["email"]);
                }
                break;
            case "logout":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId"));

                if (!$_POST['mock']) {
                    $db->logout($_POST["gId"]);
                } else {
                    $mock->logout($_POST["gId"]);
                }
                break;
            case "call":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                if (!$_POST['mock']) {
                    $db->call($_POST["gId"], $_POST["destGId"]);
                } else {
                    $mock->call($_POST["gId"]);
                }
                break;
            case "hang_up":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                if (!$_POST['mock']) {
                    $db->hangup($_POST["gId"], $_POST["destGId"]);
                } else {
                    $mock->hangup($_POST["gId"]);
                }
                break;
            case "answer":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("status", "gId", "destGId"), array(array("accept", "refuse")));

                if (!$_POST['mock']) {
                    $db->answer($_POST["gId"], $_POST["destGId"], $_POST["status"]);
                } else {
                    $mock->answer($_POST['status']);
                }
                break;
            case "delete_account":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId"));

                if (!$_POST['mock']) {
                    $db->deleteAccount($_POST['gId']);
                } else {
                    $mock->deleteAccount($_POST['gId']);
                }
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