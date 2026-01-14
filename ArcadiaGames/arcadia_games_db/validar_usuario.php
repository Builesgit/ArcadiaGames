<?php
include 'config.php';

if(isset($_POST['usuario']) && isset($_POST['password'])){
    $usuario = $_POST['usuario'];
    $password = $_POST['password'];

    $sentencia = $conexion->prepare("SELECT * FROM usuarios WHERE usuario=? AND password=?");
    $sentencia->bind_param('ss', $usuario, $password);
    $sentencia->execute();

    $resultado = $sentencia->get_result();

    if ($fila = $resultado->fetch_assoc()) {
        echo json_encode($fila, JSON_UNESCAPED_UNICODE);
    } else {
        echo "no_existe";
    }

    $sentencia->close();
} else {
    echo "Faltan parametros POST";
}

$conexion->close();
?>