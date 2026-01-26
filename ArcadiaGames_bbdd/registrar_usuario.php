<?php
    include 'config.php';

    $usuario = $_POST['usuario'];
    $password = $_POST['password'];

    $checkCount = $conexion->query("SELECT COUNT(*) as total FROM usuarios");
    $row = $checkCount->fetch_assoc();

    $rol = ($row['total'] == 0) ? 'admin' : 'user';

    $sentencia = $conexion->prepare("INSERT INTO usuarios (usuario, password, rol) VALUES (?, ?, ?)");
    $sentencia->bind_param('sss', $usuario, $password, $rol);

    if($sentencia->execute()){
        echo "registrado_exitoso";
    } else {
        echo "error";
    }
?>