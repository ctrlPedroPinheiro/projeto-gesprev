import api from './api';

export const login = async (cpf, senha) => {
  const response = await api.post('/usuarios/login', { cpf, senha });
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('usuario');
};

export const getUsuarioLogado = () => {
  const usuario = localStorage.getItem('usuario');
  return usuario ? JSON.parse(usuario) : null;
};