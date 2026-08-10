import os
from pathlib import Path
from typing import Union

from fastapi import FastAPI
from pydantic import BaseModel
from qdrant_client import QdrantClient
from sentence_transformers import SentenceTransformer

MODEL_NAME = os.getenv(
    "EMBEDDING_MODEL",
    "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
)
BASE_DIR = Path(__file__).resolve().parent
QDRANT_URL = os.getenv("QDRANT_URL")
QDRANT_PATH = Path(os.getenv("QDRANT_PATH", BASE_DIR / "qdrant_data"))
COLLECTION_NAME = os.getenv("QDRANT_COLLECTION", "legislacao_previdencia")

app = FastAPI(title="GESPREV Embeddings", version="1.0.0")
model = SentenceTransformer(MODEL_NAME)
qdrant = QdrantClient(url=QDRANT_URL) if QDRANT_URL else QdrantClient(path=str(QDRANT_PATH))


class EmbeddingRequest(BaseModel):
    input: Union[str, list[str]]
    model: str | None = None
    encoding_format: str | None = None


class BuscaRequest(BaseModel):
    consulta: str
    limit: int = 5


def perfil_consulta(consulta: str) -> str:
    texto = consulta.lower()
    termos_direito = [
        "aposentadoria", "integral", "proporcional", "proventos", "paridade",
        "integralidade", "idade", "tempo de contribuição", "tempo de contribuicao",
        "regra", "transição", "transicao", "emenda", "art. 40", "artigo 40"
    ]
    termos_administrativos = [
        "crp", "atuarial", "compensação", "compensacao", "parcelamento",
        "certificado", "investimento", "contabilidade", "demonstrativo",
        "taxa de administração", "taxa de administracao", "portaria"
    ]
    if any(termo in texto for termo in termos_administrativos):
        return "administrativo_rpps"
    if any(termo in texto for termo in termos_direito):
        return "direito_aposentadoria"
    return "geral"


def bonus_categoria(categoria: str | None, perfil: str) -> float:
    categoria = categoria or ""
    if perfil == "direito_aposentadoria":
        return {
            "constituicao": 0.22,
            "emenda_constitucional": 0.18,
            "lei_municipal": 0.14,
            "lei_complementar": 0.10,
            "lei_federal": 0.08,
            "portaria": -0.08,
        }.get(categoria, 0)
    if perfil == "administrativo_rpps":
        return {
            "portaria": 0.18,
            "lei_federal": 0.10,
            "lei_complementar": 0.06,
            "constituicao": 0.02,
            "emenda_constitucional": 0.02,
            "lei_municipal": 0.02,
        }.get(categoria, 0)
    return {
        "constituicao": 0.08,
        "emenda_constitucional": 0.07,
        "lei_municipal": 0.06,
        "lei_federal": 0.04,
        "lei_complementar": 0.04,
        "portaria": 0.00,
    }.get(categoria, 0)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "model": MODEL_NAME}


@app.post("/v1/embeddings")
def embeddings(request: EmbeddingRequest) -> dict:
    textos = [request.input] if isinstance(request.input, str) else request.input
    vetores = model.encode(textos, normalize_embeddings=True).tolist()
    return {
        "object": "list",
        "data": [
            {"object": "embedding", "embedding": vetor, "index": indice}
            for indice, vetor in enumerate(vetores)
        ],
        "model": request.model or MODEL_NAME,
        "usage": {"prompt_tokens": 0, "total_tokens": 0},
    }


@app.post("/buscar")
def buscar(request: BuscaRequest) -> dict:
    vetor = model.encode(request.consulta, normalize_embeddings=True).tolist()
    limite = max(1, min(request.limit, 10))
    pontos = qdrant.query_points(
        collection_name=COLLECTION_NAME,
        query=vetor,
        limit=min(max(limite * 4, 10), 40),
        with_payload=True,
    ).points
    perfil = perfil_consulta(request.consulta)
    reranqueados = sorted(
        pontos,
        key=lambda ponto: ponto.score + bonus_categoria(ponto.payload.get("categoria"), perfil),
        reverse=True,
    )[:limite]
    return {
        "perfil": perfil,
        "resultados": [
            {
                "fonte": ponto.payload.get("fonte"),
                "texto": ponto.payload.get("texto"),
                "score": ponto.score,
                "score_ajustado": ponto.score + bonus_categoria(ponto.payload.get("categoria"), perfil),
                "categoria": ponto.payload.get("categoria"),
                "esfera": ponto.payload.get("esfera"),
                "arquivo": ponto.payload.get("arquivo"),
            }
            for ponto in reranqueados
        ]
    }
