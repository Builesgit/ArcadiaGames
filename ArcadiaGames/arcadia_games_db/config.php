<?php
$hostname = "localhost";
$database = "arcadia_games_db";
$username = "root";
$password = "";

$conexion = new mysqli($hostname, $username, $password, $database);

if($conexion->connect_errno){
    echo "Fallo al conectar a MySQL: " . $conexion->connect_error;
}

$conexion->set_charset("utf8");
?>