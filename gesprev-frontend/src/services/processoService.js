import api from './api';

export const listarProcessos = async (filtros = {}) => {
  const response = await api.get('/processos-aposentadoria', { params: filtros });
  return response.data;
};

export const buscarProcessoPorId = async (id) => {
  const response = await api.get(`/processos-aposentadoria/${id}`);
  return response.data;
};

export const criarProcessoComServidor = async (dados) => {
  const response = await api.post('/processos-aposentadoria/com-servidor', dados);
  return response.data;
};

export const preprocessarFichaFuncionalAbertura = async (arquivo) => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  const response = await api.post('/processos-aposentadoria/preprocessar-ficha-funcional', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};

export const deletarProcesso = async (id) => {
  await api.delete(`/processos-aposentadoria/${id}`);
};

export const rejeitarProcesso = async (id, observacao) => {
  await api.patch(`/processos-aposentadoria/${id}/rejeitar`, { observacao });
};

export const reabrirProcesso = async (id, observacao) => {
  await api.patch(`/processos-aposentadoria/${id}/reabrir`, { observacao });
};

export const obterEstatisticas = async () => {
  const response = await api.get('/processos-aposentadoria/estatisticas');
  return response.data;
};

