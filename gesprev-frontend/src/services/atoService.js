import api from './api';

export const gerarAto = async (dados) => {
  const response = await api.post('/atos-aposentadoria/gerar', dados, {
    responseType: 'blob' // recebe o PDF como binário
  });
  return response.data;
};