/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Utilitat experimental per mostrar el directori d'usuari de NetBeans
 * llegint la propietat de sistema {@code netbeans.user}. Codi experimental / prototip.
 * <p>
 * [EN] Experimental utility to display the NetBeans user directory by reading
 * the {@code netbeans.user} system property. Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class NetbsUser {

    /**
     * [CA] Mostra per consola el valor de la propietat de sistema {@code netbeans.user},
     * que indica el directori d'usuari de NetBeans on es desen configuracions i logs.
     * <p>
     * [EN] Prints to the console the value of the {@code netbeans.user} system property,
     * which indicates the NetBeans user directory where settings and logs are stored.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("NetBeans User Directory: " + System.getProperty("netbeans.user"));
    }
}
