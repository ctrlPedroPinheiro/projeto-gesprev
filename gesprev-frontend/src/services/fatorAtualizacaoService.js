import api from './api';

export const verificarFatoresExistentes = async () => {
  const response = await api.get('/fatores-atualizacao/verificar');
  return response.data;
};

export const importarFatores = async (arquivo, portaria) => {
  const formData = new FormData();
  formData.append('arquivo', arquivo);
  formData.append('portaria', portaria);
  const response = await api.post('/fatores-atualizacao/importar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return response.data;
};
