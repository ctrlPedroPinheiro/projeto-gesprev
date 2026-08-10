import { useState } from 'react';
import { login as loginService, logout as logoutService, getUsuarioLogado } from '../services/authService';
import { AuthContext } from './useAuth';

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(getUsuarioLogado());

  const login = async (cpf, senha) => {
    const data = await loginService(cpf, senha);
    localStorage.setItem('token', data.token);
    localStorage.setItem('usuario', JSON.stringify(data.usuario));
    setUsuario(data.usuario);
    return data.usuario;
  };

  const logout = () => {
    logoutService();
    setUsuario(null);
  };

  return (
    <AuthContext.Provider value={{ usuario, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
