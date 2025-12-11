package com.example.arcadiagames;

import java.util.ArrayList;
import java.util.List;

public class DatosUsuarios {
    private static List<Usuarios> usuarios = new ArrayList<>();

    public static List<Usuarios> getUsuarios() {
        return usuarios;
    }

    public static void addUsuario(Usuarios usuario) {
        usuarios.add(usuario);
    }

    public static Usuarios findUsuario(String nombre_usuario, String contraseña) {
        for (Usuarios usuario : usuarios) {
            if (usuario.getNombre_usuario().equals(nombre_usuario) && usuario.getContraseña().equals(contraseña)) {
                return usuario;
            }
        }
        return null;
    }
    public static boolean UsuarioEstaRegistrado(String nombre_usuario) {
        for (Usuarios usuario : usuarios) {
            if (usuario.getNombre_usuario().equals(nombre_usuario)) {
                return true;
            }
        }
        return false;
    }
}
