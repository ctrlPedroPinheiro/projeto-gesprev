import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RotaProtegida from './RotaProtegida';
import { useAuth } from '../contexts/useAuth';

vi.mock('../contexts/useAuth', () => ({ useAuth: vi.fn() }));

function renderizarRota(perfil) {
  render(
    <MemoryRouter initialEntries={['/restrita']}>
      <Routes>
        <Route path="/login" element={<p>Página de login</p>} />
        <Route path="/dashboard" element={<p>Dashboard</p>} />
        <Route path="/restrita" element={(
          <RotaProtegida perfil={perfil}>
            <p>Conteúdo restrito</p>
          </RotaProtegida>
        )} />
      </Routes>
    </MemoryRouter>
  );
}

describe('RotaProtegida', () => {
  beforeEach(() => vi.clearAllMocks());

  it('redireciona visitantes sem sessão para o login', () => {
    useAuth.mockReturnValue({ usuario: null });
    renderizarRota();

    expect(screen.getByText('Página de login')).toBeInTheDocument();
  });

  it('redireciona um perfil sem permissão para o dashboard', () => {
    useAuth.mockReturnValue({ usuario: { perfil: 'ANALISTA' } });
    renderizarRota('DIRETOR');

    expect(screen.getByText('Dashboard')).toBeInTheDocument();
  });

  it('renderiza o conteúdo para o perfil autorizado', () => {
    useAuth.mockReturnValue({ usuario: { perfil: 'DIRETOR' } });
    renderizarRota('DIRETOR');

    expect(screen.getByText('Conteúdo restrito')).toBeInTheDocument();
  });
});
