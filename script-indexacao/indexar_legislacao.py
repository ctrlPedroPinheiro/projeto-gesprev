import argparse
import os
import uuid
from pathlib import Path

import pypdf
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, PointStruct, VectorParams
from sentence_transformers import SentenceTransformer

QDRANT_URL = os.getenv("QDRANT_URL")
COLLECTION_NAME = os.getenv("QDRANT_COLLECTION", "legislacao_previdencia")
MODEL_NAME = os.getenv(
    "EMBEDDING_MODEL",
    "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
)
VECTOR_SIZE = 384
BASE_DIR = Path(__file__).resolve().parent
LEGISLACAO_DIR = Path(os.getenv("LEGISLACAO_DIR", BASE_DIR / "leis"))
QDRANT_PATH = Path(os.getenv("QDRANT_PATH", BASE_DIR / "qdrant_data"))


def classificar_documento(fonte: str) -> tuple[str, str, int]:
    nome = fonte.upper()
    if nome.startswith("ART-40") or "CONSTITUICAO" in nome or "CONSTITUIÇÃO" in nome:
        return "constituicao", "federal", 100
    if nome.startswith("EC-"):
        return "emenda_constitucional", "federal", 95
    if nome.startswith("LC-"):
        return "lei_complementar", "federal", 85
    if nome.startswith("L-"):
        return "lei_federal", "federal", 80
    if nome.startswith("PORTARIA"):
        return "portaria", "federal", 35
    if nome.startswith("LEI-"):
        return "lei_municipal", "municipal", 90
    return "legislacao", "indefinida", 50


def extrair_texto_pdf(caminho_pdf: Path) -> str:
    partes = []
    with caminho_pdf.open("rb") as arquivo:
        reader = pypdf.PdfReader(arquivo)
        for pagina in reader.pages:
            texto = pagina.extract_text()
            if texto:
                partes.append(texto)
    return "\n".join(partes)


def dividir_em_chunks(texto: str, tamanho: int = 500, sobreposicao: int = 50) -> list[str]:
    palavras = texto.split()
    passo = tamanho - sobreposicao
    return [" ".join(palavras[i : i + tamanho]) for i in range(0, len(palavras), passo)]


def garantir_collection(client: QdrantClient) -> None:
    if not client.collection_exists(COLLECTION_NAME):
        client.create_collection(
            collection_name=COLLECTION_NAME,
            vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
        )
        print(f"Collection '{COLLECTION_NAME}' criada.")


def criar_cliente() -> QdrantClient:
    if QDRANT_URL:
        return QdrantClient(url=QDRANT_URL)
    return QdrantClient(path=str(QDRANT_PATH))


def indexar_documento(
    client: QdrantClient,
    model: SentenceTransformer,
    caminho_pdf: Path,
) -> None:
    fonte = caminho_pdf.stem
    categoria, esfera, prioridade = classificar_documento(fonte)
    print(f"Indexando {fonte}...")
    chunks = dividir_em_chunks(extrair_texto_pdf(caminho_pdf))
    if not chunks:
        print("  Documento sem texto extraivel; ignorado.")
        return

    embeddings = model.encode(chunks, normalize_embeddings=True).tolist()
    pontos = [
        PointStruct(
            id=str(uuid.uuid5(uuid.NAMESPACE_URL, f"{fonte}:{indice}:{chunk}")),
            vector=embedding,
            payload={
                "texto": chunk,
                "fonte": fonte,
                "arquivo": caminho_pdf.name,
                "chunk_index": indice,
                "categoria": categoria,
                "esfera": esfera,
                "prioridade": prioridade,
            },
        )
        for indice, (chunk, embedding) in enumerate(zip(chunks, embeddings))
    ]
    client.upsert(collection_name=COLLECTION_NAME, points=pontos, wait=True)
    print(f"  {len(pontos)} chunks indexados.")


def arquivos_padrao() -> list[Path]:
    return sorted(LEGISLACAO_DIR.rglob("*.pdf"))


def main() -> None:
    parser = argparse.ArgumentParser(description="Indexa legislacao previdenciaria no Qdrant.")
    parser.add_argument("arquivos", nargs="*", type=Path, help="PDFs que serao indexados")
    args = parser.parse_args()
    arquivos = [arquivo.resolve() for arquivo in args.arquivos] or arquivos_padrao()

    if not arquivos:
        raise FileNotFoundError(f"Nenhum PDF encontrado em {LEGISLACAO_DIR}")

    inexistentes = [str(arquivo) for arquivo in arquivos if not arquivo.is_file()]
    if inexistentes:
        raise FileNotFoundError("PDFs nao encontrados: " + ", ".join(inexistentes))

    model = SentenceTransformer(MODEL_NAME)
    client = criar_cliente()
    garantir_collection(client)
    for arquivo in arquivos:
        indexar_documento(client, model, arquivo)
    print("Indexacao concluida.")


if __name__ == "__main__":
    main()
