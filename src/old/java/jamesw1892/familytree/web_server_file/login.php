<?php
    session_start();
?>

<!DOCTYPE html>
<html>

<head>
    <title>Login</title>
</head>

<body>

    <h1>Login</h1>
    <form action="" method="POST">
        <label>Password: </label><input name="password" type="password">
        <br>
        <input type="submit" value="Login">
    </form>

    <?php
        if ($_SERVER["REQUEST_METHOD"] == "POST") {
            // username and password sent from form
            if ($_POST["password"] === "thepassword") {
                // echo "successful login";
                $_SESSION["login_user"] = "hasaccess";
                // if (isset($_SESSION["login_user"])) {
                //     echo "set";
                // } else {
                //     echo "not set";
                // }
                header("location: index.php");
            } else {
                echo "<p>Invalid password</p>";
            }
        }
    ?>

</body>

</html>