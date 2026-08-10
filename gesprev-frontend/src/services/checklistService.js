import api from './api';

export const listarChecklistPorProcesso = async (processoId) => {
  const response = await api.get(`/checklist-documentos/processo/${processoId}`);
  return response.data;
};
