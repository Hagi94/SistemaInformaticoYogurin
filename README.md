# Sistema Informático Yogurín

Aplicación Java Swing para gestión de usuarios, clientes, productos, producción e inventario.

## Configuración rápida
1. Restaurar la BD:
   ```bash
   mysql -u root -P 3306 < database/yogurin_bustamantedb.sql
   ```
2. Verificar conexión en `src/Utilidades/Constantes.java`.
3. Ejecutar desde NetBeans/Ant.

## Mejoras incluidas
- Seguridad de usuarios con SHA-256 + salt.
- DAO refactorizados con `try-with-resources`.
- Validaciones reutilizables en `src/Utilidades/Validaciones.java`.
- Login con validación de campos y ruta de imagen relativa.
