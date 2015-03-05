<?php

require_once("utils/FilesManager.php");
require_once("utils/Utils.php");
require_once("Mock.php");

try {
    $display = array();

    if ($_SERVER["REQUEST_METHOD"] == 'GET') {
        // Set the mock
        $mock = new Mock(isset($_GET['mock']) && $_GET['mock'] == "true");

        // Chech if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_GET, "action", array(array("get_online_users", "connection_status", "who_is_calling_me")));

        switch ($_GET['action']) {
            case "get_online_users":
                // TODO get all online users and add it in $display

                $display = array_merge($display, $mock->addOnlineUsers());
                break;
            case "connection_status":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId", "destGId"));

                // TODO
                break;
            case "who_is_calling_me":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_GET, array("gId"));

                // TODO
                $display = array_merge($display, $mock->addWhoIsCallingMe());
                break;
        }
    } else if ($_SERVER["REQUEST_METHOD"] == "POST") {
        // Chech if the parameters are present and correct. Throw an exception otherwise
        Utils::checkParams($_POST, "action", array(array("add_user", "name", "connect_to_user", "close_connection", "answer")));

        // Set the mock
        $mock = new Mock(isset($_POST['mock']) && $_POST['mock'] == "true");

        switch ($_POST['action']) {
            case "add_user":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "name", "image", "publicKey"));

                // TODO
                break;
            case "login":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId"));

                // TODO
                break;
            case "connect_to_user":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                // TODO
                break;
            case "close_connection":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
                Utils::checkParams($_POST, array("gId", "destGId"));

                // TODO
                break;
            case "answer":
                // Chech if the parameters are present and not empty. Throw an exception otherwise
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