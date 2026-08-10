import api from './api';

export const listarServidores = async () => {
  const response = await api.get('/servidores');
  return response.data;
};

export const buscarServidorPorId = async (id) => {
  const response = await api.get(`/servidores/${id}`);
  return response.data;
};

export const atualizarServidor = async (id, dados) => {
  const response = await api.put(`/servidores/${id}`, dados);
  return response.data;
};

