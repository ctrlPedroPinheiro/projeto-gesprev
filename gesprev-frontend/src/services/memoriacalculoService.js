import api from './api';

export const calcular = async (processoId, tipoCalculo) => {
  const response = await api.post(`/memorias-calculo/calcular/${processoId}`, null, {
    params: { tipoCalculo }
  });
  return response.data;
};

export const obterMemoriaPorProcesso = async (processoId) => {
  const response = await api.get(`/memorias-calculo/processo/${processoId}`);
  return response.data;
};