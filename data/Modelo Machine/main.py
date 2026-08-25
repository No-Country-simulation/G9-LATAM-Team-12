from pathlib import Path
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pandas as pd

app = FastAPI(
    title="API EnergiAI - Predicción de Eficiencia Energética", version="1.0.0"
)

# --- 1. CARGA SEGURA DEL MODELO ---
BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "energiai_model.joblib"

try:
    model = joblib.load(MODEL_PATH)
    print("✅ Modelo cargado correctamente en FastAPI")
except Exception as e:
    model = None
    print(f"❌ Error al cargar el modelo: {e}")


# --- 2. ESQUEMAS DE ENTRADA Y SALIDA ---
class InmuebleRequest(BaseModel):
    consumo_kwh: float
    uso_horario_pico: bool
    cantidad_equipos: int
    tipo_inmueble: str
    horas_alto_consumo: float


class InferenciaResponse(BaseModel):
    categoria: str
    probabilidad: float


# --- Mapeo de compatibilidad ---
# El modelo fue entrenado con las categorías ['Apartamento', 'Casa', 'Comercial']
# para 'tipo_inmueble', pero el frontend envía 'Departamento'. Normalizamos acá
# para no depender de resincronizar frontend y modelo cada vez que cambie una etiqueta.
TIPO_INMUEBLE_ALIASES = {
    "Departamento": "Apartamento",
}


# --- 3. ENDPOINT DE PREDICCIÓN ---
@app.post("/predict", response_model=InferenciaResponse)
def predict(data: InmuebleRequest):
    if model is None:
        raise HTTPException(
            status_code=500,
            detail="Modelo no disponible en el servidor. Revisa que energiai_model.joblib esté en la misma carpeta.",
        )

    try:
        # Convertir la petición JSON a DataFrame
        payload = data.model_dump()
        payload["tipo_inmueble"] = TIPO_INMUEBLE_ALIASES.get(
            payload["tipo_inmueble"], payload["tipo_inmueble"]
        )
        df_input = pd.DataFrame([payload])

        # Feature Engineering (crear variable derivada)
        df_input["consumo_por_equipo"] = (
            df_input["consumo_kwh"] / df_input["cantidad_equipos"]
        )

        # Inferencia con el modelo
        categoria = model.predict(df_input)[0]
        probabilidades = model.predict_proba(df_input)[0]

        return {
            "categoria": str(categoria),
            "probabilidad": round(float(max(probabilidades)), 4),
        }
    except Exception as e:
        raise HTTPException(
            status_code=400,
            detail=f"Error al procesar la inferencia: {type(e).__name__} - {str(e)}",
        )