# Base de Datos Yogurín

## Requisitos
- MySQL 8+
- Usuario: `root`
- Contraseña: *(vacía)*
- Puerto: `3306`

## Crear la base de datos
```bash
mysql -u root -P 3306 < database/yogurin_bustamantedb.sql
```

## Credenciales iniciales
- Usuario: `admin`
- Clave original: `1234`  
  (en BD se guarda con hash SHA-256 + salt)

## Notas
- El script crea índices, constraints, claves foráneas y triggers de auditoría.
- Se incluyen datos de prueba para usuarios, clientes, productos e insumos.
