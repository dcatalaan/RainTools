# RainTools

Un plugin de Minecraft que permite crear áreas de lluvia personalizadas donde pueden caer items, orbes de experiencia y entidades.

## Características

- ✨ Crea áreas de lluvia personalizadas con límites específicos
- 🎁 Configura items para que caigan con probabilidades personalizadas
- ⭐ Agrega orbes de experiencia con cantidades configurables
- 🌟 Áreas de lluvia de un solo uso o repetitivas
- 🧹 Limpieza automática de entidades después de 20 minutos
- 🌍 Soporte multi-idioma (Inglés y Español)
- 💻 Soporte para comandos desde consola
- 📝 Mensajes personalizables

## Requisitos

- Servidor Spigot/Paper 1.16.5 o superior
- Java 8 o superior

## Instalación

1. Descarga el archivo JAR desde la sección de [Releases](https://github.com/dcatalaan/RainTools/releases)
2. Coloca el archivo JAR en la carpeta `plugins` de tu servidor
3. Reinicia el servidor
4. ¡Listo para usar!

## Comandos

### Gestión de Áreas
- `/rainarea create <nombre>` - Crear nueva área de lluvia
- `/rainarea delete <nombre>` - Eliminar un área existente
- `/rainarea list` - Listar todas las áreas configuradas
- `/rainarea start <nombre>` - Iniciar lluvia en un área
- `/rainarea stop <nombre>` - Detener lluvia en un área
- `/rainarea info <nombre>` - Mostrar información del área
- `/rainarea config <área> <propiedad> <valor>` - Configurar propiedades del área
- `/rainarea reload` - Recargar configuración

### Gestión de Items
- `/saveitem <nombre>` - Guardar item en mano
- `/saveitem list` - Listar items guardados
- `/saveitem delete <nombre>` - Eliminar un item guardado
- `/saveitem add <área> <item> <probabilidad>` - Agregar item a un área
- `/saveitem removearea <área> <item>` - Quitar item de un área

## Permisos

- `raintools.area.create` - Crear áreas de lluvia
- `raintools.area.delete` - Eliminar áreas de lluvia
- `raintools.area.manage` - Gestionar áreas y configuración
- `raintools.item.save` - Guardar y gestionar items

## Licencia

Este proyecto está bajo la licencia Apache 2.0 - ver el archivo [LICENSE](LICENSE) para más detalles.

## Autor

- [@dcatalaan](https://github.com/dcatalaan) 