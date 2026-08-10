import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import { Box, Grid, Paper, Typography, CircularProgress, Button } from '@mui/material';
import FolderIcon from '@mui/icons-material/Folder';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import PendingIcon from '@mui/icons-material/Pending';
import AnalyticsIcon from '@mui/icons-material/Analytics';
import AddIcon from '@mui/icons-material/Add';
import { obterEstatisticas } from '../../services/processoService';
import { useAuth } from '../../contexts/useAuth';
import AssistenteChat from '../../components/AssistenteChat/AssistenteChat';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const { usuario } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    let ativo = true;

    const carregarEstatisticas = async () => {
      try {
        const data = await obterEstatisticas();
        if (ativo) setStats(data);
      } catch {
        if (ativo) setStats({ total: 0, finalizados: 0, pendentes: 0, emAnalise: 0 });
      } finally {
        if (ativo) setCarregando(false);
      }
    };

    carregarEstatisticas();
    return () => { ativo = false; };
  }, []);

  const cards = [
    {
      titulo: 'Total de Processos',
      valor: stats?.total ?? '-',
      icone: <FolderIcon fontSize="large" color="primary" />,
      rota: '/processos'
    },
    {
      titulo: 'Em Análise',
      valor: stats?.emAnalise ?? '-',
      icone: <AnalyticsIcon fontSize="large" color="info" />,
      rota: '/processos'
    },
    {
      titulo: 'Pendentes',
      valor: stats?.pendentes ?? '-',
      icone: <PendingIcon fontSize="large" color="warning" />,
      rota: '/processos'
    },
    {
      titulo: 'Finalizados',
      valor: stats?.finalizados ?? '-',
      icone: <CheckCircleIcon fontSize="large" color="success" />,
      rota: '/processos'
    },
  ];

  return (
    <Layout>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">Dashboard</Typography>
          <Typography variant="body2" color="text.secondary">
            Bem-vindo, {usuario?.nome}
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => navigate('/processos?novo=true')}
        >
          Novo Processo
        </Button>
      </Box>

      {carregando ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={3}>
          {cards.map((card) => (
            <Grid size={{ xs: 12, sm: 6, md: 3 }} key={card.titulo}>
              <Paper
                elevation={2}
                sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2, cursor: 'pointer',
                  '&:hover': { elevation: 4, bgcolor: 'action.hover' } }}
                onClick={() => navigate(card.rota)}
              >
                {card.icone}
                <Box>
                  <Typography variant="h4" fontWeight="bold">{card.valor}</Typography>
                  <Typography variant="body2" color="text.secondary">{card.titulo}</Typography>
                </Box>
              </Paper>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Processos recentes */}
      <Typography variant="h6" fontWeight="bold" sx={{ mt: 4, mb: 2 }}>
        Acesso Rápido
      </Typography>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Paper
            elevation={1}
            sx={{ p: 2, cursor: 'pointer', textAlign: 'center', '&:hover': { bgcolor: 'action.hover' } }}
            onClick={() => navigate('/processos')}
          >
            <Typography variant="body1" fontWeight="bold">Processos</Typography>
            <Typography variant="body2" color="text.secondary">Ver todos os processos</Typography>
          </Paper>
        </Grid>
        {usuario?.perfil === 'DIRETOR' && (
          <Grid size={{ xs: 12, sm: 6 }}>
            <Paper
              elevation={1}
              sx={{ p: 2, cursor: 'pointer', textAlign: 'center', '&:hover': { bgcolor: 'action.hover' } }}
              onClick={() => navigate('/usuarios')}
            >
              <Typography variant="body1" fontWeight="bold">Usuários</Typography>
              <Typography variant="body2" color="text.secondary">Gerenciar usuários</Typography>
            </Paper>
          </Grid>
        )}
      </Grid>

      <Box sx={{ mt: 4 }}>
        <AssistenteChat
          titulo="Assistente do GESPREV"
          altura={420}
          mensagemInicial="Olá! Posso responder dúvidas técnicas sobre legislação previdenciária, regras de aposentadoria e estatísticas/processos do GESPREV."
        />
      </Box>
    </Layout>
  );
}
