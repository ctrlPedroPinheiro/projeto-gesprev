import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from './AuthContext';
import { useAuth } from './useAuth';
import { getUsuarioLogado, login as loginService, logout as logoutService } from '../services/authService';

vi.mock('../services/authService', () => ({
  getUsuarioLogado: vi.fn(),
  login: vi.fn(),
  logout: vi.fn()
}));

describe('AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    getUsuarioLogado.mockReturnValue(null);
  });

  it('armazena token e usuário após autenticação', async () => {
    const usuario = { id: 1, nome: 'Diretor', perfil: 'DIRETOR' };
    loginService.mockResolvedValue({ token: 'jwt-teste', usuario });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await act(async () => result.current.login('900.000.000-00', 'senha123'));

    expect(loginService).toHaveBeenCalledWith('900.000.000-00', 'senha123');
    expect(localStorage.getItem('token')).toBe('jwt-teste');
    expect(JSON.parse(localStorage.getItem('usuario'))).toEqual(usuario);
    expect(result.current.usuario).toEqual(usuario);
  });

  it('encerra a sessão e limpa o usuário do contexto', () => {
    getUsuarioLogado.mockReturnValue({ id: 1, perfil: 'DIRETOR' });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    act(() => result.current.logout());

    expect(logoutService).toHaveBeenCalledOnce();
    expect(result.current.usuario).toBeNull();
  });
});
