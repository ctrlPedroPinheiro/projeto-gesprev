import api from './api';

export const enviarMensagem = async (pergunta, numeroProcesso = null) => {
  const response = await api.post('/chat', { pergunta, numeroProcesso });
  return response.data;
};
