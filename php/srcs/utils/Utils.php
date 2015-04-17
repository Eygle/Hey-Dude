<?php
/**
 * Created by PhpStorm.
 * User: Johan
 * Date: 09/02/2015
 * Time: 18:20
 */

class Utils {
    public static function checkParams($type, $params, $val = null) {
        if (is_array($params)) {
            foreach ($params as $i => $p) {
                Utils::checkParam($type, $p, $val, $i);
            }
        } else {
            Utils::checkParam($type, $params, $val);
        }
    }

    public static function checkPassword($password) {
        if ($password != "b1889094079e73358b72111e680ff087") {
            throw new Exception("Bad password.");
        }
    }

    public static function sendOnlineUsersList($db, $gId = null) {
        $tokens = $db->getOnlineUsersTokens();
        if (count($tokens) > 0) {
            self::sendPush($tokens, array("action" => "refresh_user_list", "list" => json_encode($db->onlineUsers($gId))));
        }
    }

    public static function sendPush($tokens, $data) {
        // Set POST variables
        $url = 'https://android.googleapis.com/gcm/send';

        $fields = array(
            'registration_ids' => $tokens,
            'data' => $data,
        );

        $headers = array(
            'Authorization: key=AIzaSyBqSPVp50G_CYQEVAuinfaD8KQjqZSbycA',
            'Content-Type: application/json'
        );
        //print_r($headers);
        // Open connection
        $ch = curl_init();

        // Set the url, number of POST vars, POST data
        curl_setopt($ch, CURLOPT_URL, $url);

        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

        // Disabling SSL Certificate support temporarly
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

        // Execute post
        $result = curl_exec($ch);
        if ($result === FALSE) {
            die('Curl failed: ' . curl_error($ch));
        }

        // Close connection
        curl_close($ch);
        echo $result;
    }

    private static function checkParam($t, $p, $val, $i = 0) {
        if (!isset($t[$p]) || empty($t[$p])) {
            throw new Exception("Parameter $p do not exist or is empty.");
        }

        if ($val && is_array($val) && $i < count($val)) {
            Utils::checkVal($t, $p, $val[$i]);
        } else if ($val && !is_array($val)) {
            Utils::checkVal($t, $p, $val);
        }
    }

    private static function checkVal($t, $p, $val) {
        if (is_array($val) && !in_array($t[$p], $val)) {
            throw new Exception("Parameter $p should be equal to one of those values: ".implode(", ", $val).".");
        } else if (!is_array($val) && $t[$p] != $val) {
            throw new Exception("Parameter $p should be equal to $val.");
        }
    }
}