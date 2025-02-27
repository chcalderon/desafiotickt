# Guía de Instalación y Uso

## Requisitos Previos

Antes de instalar y ejecutar la aplicación, asegúrese de tener lo siguiente instalado en su sistema:

- **Java 17 o superior**
- **Maven** (para compilar y gestionar dependencias)
- **Git** (opcional, para clonar el repositorio)

## Instalación

### 1. Clonar el repositorio
```sh
 git clone https://github.com/chcalderon/desafiotickt.git
 cd desafiotickt/svc
```

### 2. Configurar Gmail para el envío de correos
1. Ingrese a [Google Account Security](https://myaccount.google.com/security)
2. Habilite la verificación en dos pasos
3. Genere una **contraseña de aplicación** en "Contraseñas de Aplicaciones"
4. Copie la contraseña generada y configúrela en `application.properties` ubicada en la ruta svc/src/main/resources, debe quedar como se muestra a continuación:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tuemail@gmail.com
spring.mail.password=contraseña_generada
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Construir y ejecutar la aplicación

#### 3.1 Compilar y empaquetar
```sh
mvn clean package
```

#### 3.2 Ejecutar la aplicación
```sh
java -jar target/svc-0.0.1-SNAPSHOT.jar
```

## Uso de la Aplicación
1. Abra un navegador y acceda a `http://localhost:8080`
2. Cargue un archivo y defina el tamaño de segmentación
3. Una vez procesado, podrá:
   - Descargar los segmentos generados
   - Recombinarlos en un solo archivo ó Enviarlos por correo electrónico

## Problemas Comunes

- **Correo no enviado**: Revise la configuración SMTP y asegúrese de que la contraseña de aplicación de Gmail es válida.
- **Puerto en uso**: Si `8080` está ocupado, puede cambiarlo en `application.properties` con:
  ```properties
  server.port=9090
  
