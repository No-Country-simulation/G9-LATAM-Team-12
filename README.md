# G9-LATAM-Team-12 - EnergiAI

<p align="center">
   <img src="http://img.shields.io/static/v1?label=ESTADO&message=EN%20DESARROLLO&color=YELLOW&style=for-the-badge"/>
</p>

### Índice

- [Descripción del proyecto](#descripción-del-proyecto)
- [Funcionalidades](#funcionalidades)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Desarrolladores](#desarrolladores)

## Descripción del proyecto

<p align="justify">
EnergiAI es una solución inteligente desarrollada como parte del Hackathon para analizar patrones de consumo de energía eléctrica y generar información que ayude en la toma de decisiones relacionadas con la eficiencia energética. La solución recibe datos como consumo mensual en kWh, horarios de mayor utilización, cantidad de equipos y perfil de uso, para luego clasificar el perfil energético en categorías (Eficiente, Moderado, Ineficiente) y ofrecer recomendaciones para reducir el desperdicio energético.
</p>

## Funcionalidades

:heavy_check_mark: `Análisis de consumo:` Endpoint POST /analisis-energetico que recibe datos de consumo y retorna clasificación del perfil energético.

:heavy_check_mark: `Recomendaciones:` Generación de sugerencias para optimizar el consumo energético.

:heavy_check_mark: `Estimación financiera:` Cálculo del costo estimado mensual basado en tarifa de referencia.

:heavy_check_mark: `Documentación:` Documentación interactiva de la API disponible vía Swagger UI.

:heavy_check_mark: `Integración con OCI:` Utilización de Object Storage para almacenamiento de modelos o datos.

## Tecnologías utilizadas

<div style="display: flex; align-items: center; gap: 8px;">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="40" height="40" alt="Java 21"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="40" height="40" alt="Spring Boot 4.1.0"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" width="40" height="40" alt="Maven"/>
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/oracle/oracle-original.svg" width="40" height="40" alt="OCI"/>
</div>

- **Java 21**
- **Spring Boot 4.1.0** (Web, Validation)
- **Lombok** para generar automáticamente getters, setters y constructores
- **SpringDoc OpenAPI** (Swagger UI) para documentación de la API
- **OCI Object Storage** para almacenamiento de modelos o datos
- **Maven** como gestor de dependencias

## Estructura del proyecto

```
> Estructura objetivo del proyecto.

G9-LATAM-Team-12/
└── backend/
    ├── src/
    │   ├── main/
    │   │   ├── controller/   # Endpoints de la API
    │   │   ├── service/      # Lógica de negocio
    │   │   ├── dto/          # DTOs de entrada y salida
    │   │   ├── entity/       # Entidades JPA
    │   │   └── resources/    # application.properties y recursos
    └── pom.xml               # Dependencias y configuración Maven
```

## Desarrolladores

| [<img src="https://avatars.githubusercontent.com/u/131219272?v=4" width="115"><br><sub>Ariel Cruz</sub>](https://github.com/leoxz-dev) | [<img src="https://avatars.githubusercontent.com/u/52932892?v=4" width="115"><br><sub>Mariana Ponce Sepúlveda</sub>](https://github.com/mariana-ponce-sepulveda) | [<img src="https://avatars.githubusercontent.com/u/104871112?v=4" width="115"><br><sub>Tabaré Sánchez</sub>](https://github.com/Tabare96) |
| :---: | :---: | :---: |
| Backend | Backend | Backend |

|  [<img src="https://avatars.githubusercontent.com/u/234071987?v=4" width="115"><br><sub>Alejandro Guerrero</sub>](https://github.com/okayguerrero11) | [<img src="https://avatars.githubusercontent.com/u/136405891?v=4" width="115"><br><sub>Bruno Ortiz</sub>](https://github.com/JustKarlos) | [<img src="https://avatars.githubusercontent.com/u/230570891?v=4" width="115"><br><sub>Guadalupe</sub>](https://github.com/guadalupeltissera-png) |
| :---: | :---: | :---: |
| Data Science | Data Science | Data Science |
