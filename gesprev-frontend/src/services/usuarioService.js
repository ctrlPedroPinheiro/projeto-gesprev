import api from './api';

export const listarUsuarios = async () => {
  const response = await api.get('/usuarios');
  return response.data;
};

export const criarUsuario = async (usuario) => {
  const response = await api.post('/usuarios', usuario);
  return response.data;
};

export const atualizarUsuario = async (id, usuario) => {
  const response = await api.put(`/usuarios/${id}`, usuario);
  return response.data;
};

export const deletarUsuario = async (id) => {
  await api.delete(`/usuarios/${id}`);
};