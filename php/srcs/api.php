<?php

require_once("utils/FilesManager.php");
require_once("utils/Utils.php");
require_once("Mock.php");
require_once("db/DBApi.php");

try {
    $display = array();
    $db = new DBApi();

    if ($_SERVER["REQUEST_METHOD"] == 'GET') {
        // Set the mock
        $mock = new Mock(isset($_GET['mock']) && $_GET['mock'] == "true");

        // Check if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_GET, "action", array(array("get_online_users", "call_status", "who_is_calling_me")));

        switch ($_GET['action']) {
            case "get_online_users":
                // TODO get all online users and add it in $display

                $display = array_merge($display, $mock->addOnlineUsers());
                break;
            case "call_status":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId", "destGId"));

                // TODO
                break;
            case "who_is_calling_me":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId"));

                // TODO
                $display = array_merge($display, $mock->addWhoIsCallingMe());
                break;
        }
    } else if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Check if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_POST, "action", array(array("add_user", "name", "call", "hangup", "answer")));

        // Set the mock
        $mock = new Mock(isset($_POST['mock']) && $_POST['mock'] == "true");

        switch ($_POST['action']) {
            case "login":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "name", "image", "email", "publicKey"));

                $db->login($_POST["gId"], $_POST["name"], $_POST["image"], $_POST["email"], $_POST["publicKey"], $_SERVER["REMOTE_ADDR"]);
                break;
            case "logout":
                Utils::checkParams($_POST, array("gId"));

                break;
            case "call":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                // TODO
                break;
            case "hangup":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                // TODO
                break;
            case "answer":
                // Check if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("status", "gId", "destGId"), array(array("accept", "refuse")));

                // TODO
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