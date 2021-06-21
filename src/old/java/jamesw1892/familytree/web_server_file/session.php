<?php
    session_start();
    // This file should be called from all pages that need you to be logged in.
    // If they are not logged in, it redirects them to the login page.
    // echo "executed";
    if (!isset($_SESSION["login_user"])){
        // echo "redirected";
        header("location: login.php");
        // die();
    }
?>