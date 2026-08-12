package com.veterinaria.util;

public class TextoUtil {

    private TextoUtil() {
    }

    /**
     * Regla de negocio: normaliza un nombre a formato título: cada palabra
     * con la primera letra en mayúscula y el resto en minúscula
     * ("juan perez" -> "Juan Perez"). Respeta acentos y limpia espacios.
     */
    public static String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto == null ? null : texto.trim();
        }

        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }
            if (palabra.isEmpty()) {
                continue;
            }
            resultado.append(Character.toUpperCase(palabra.charAt(0)));
            if (palabra.length() > 1) {
                resultado.append(palabra.substring(1).toLowerCase());
            }
        }

        return resultado.toString();
    }
}
