import api from './api';

export const listarHistoricoPorProcesso = async (processoId) => {
  const response = await api.get(`/historicos-processo/processo/${processoId}`);
  return response.data;
};