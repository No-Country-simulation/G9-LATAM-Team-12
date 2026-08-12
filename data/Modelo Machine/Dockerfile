# Usar una imagen oficial de Python delgada
FROM python:3.11-slim

# Establecer directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar e instalar dependencias primero (aprovecha la caché de Docker)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el resto del código y el modelo al contenedor
COPY . .

# Exponer el puerto 8000 donde corre FastAPI
EXPOSE 8000

# Comando para arrancar el servidor escuchando en todas las interfaces de red (0.0.0.0)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]