# Usamos la imagen oficial y ultraliviana de Nginx
FROM nginx:alpine

# Copiamos todo el contenido del frontend al directorio público de Nginx
COPY . /usr/share/nginx/html

# Exponemos el puerto estándar web HTTP
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]