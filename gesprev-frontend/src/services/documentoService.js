import api from './api';

export const listarDocumentosPorProcesso = async (processoId) => {
  const response = await api.get(`/documentos/processo/${processoId}`);
  return response.data;
};

export const deletarDocumento = async (id) => {
  await api.delete(`/documentos/${id}`);
};

export const processarDocumento = async (id) => {
  const response = await api.post(`/vlm/processar/${id}`);
  return response.data;
};

export const confirmarDados = async (id) => {
  await api.patch(`/vlm/validar/${id}`);
};

export const atualizarJsonExtraido = async (id, jsonExtraido) => {
  const response = await api.patch(`/documentos/${id}/json-extraido`, { jsonExtraido });
  return response.data;
};

export const uploadDocumento = async (processoId, arquivo, tipoDocumento) => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  formData.append('processoId', processoId);
  formData.append('tipoDocumento', tipoDocumento);
  const response = await api.post('/documentos/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};

export const downloadDocumento = async (id, nomeOriginal) => {
  const response = await api.get(`/documentos/download/${id}`, {
    responseType: 'blob'
  });
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = nomeOriginal;
  link.click();
  window.URL.revokeObjectURL(url);
};
