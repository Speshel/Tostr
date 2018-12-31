<?php
    ini_set('display_errors', 1);
    ini_set('display_startup_errors', 1);
    error_reporting(E_ALL);

    require('module.php');
    require('postTime.php');

    $command = $_POST['command'];

    switch ($command) {
        case 'PostTime':
            $fn = $_POST['fn'];
            post();
            break;
        case 'ListPosted':
            $res = listFn();
            echo json_encode($res);
            break;
        default:
            echo "<script>console.log('Unknown Command');</script>";
            break;
    }
?>