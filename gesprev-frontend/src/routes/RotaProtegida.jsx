import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';

export default function RotaProtegida({ children, perfil }) {
  const { usuario } = useAuth();

  if (!usuario) return <Navigate to="/login" />;
  if (perfil && usuario.perfil !== perfil) return <Navigate to="/dashboard" />;

  return children;
}
