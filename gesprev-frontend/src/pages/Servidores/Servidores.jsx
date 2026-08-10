import { useState, useEffect, useCallback } from 'react';
import Layout from '../../components/Layout/Layout';
import {
  Alert, CircularProgress, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material';
import { listarServidores } from '../../services/servidorService';
import { obterMensagemErro } from '../../utils/apiError';

export default function Servidores() {
  const [servidores, setServidores] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  const carregarServidores = useCallback(async () => {
    try {
      const data = await listarServidores();
      setServidores(data);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao carregar servidores'));
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    const carregarInicial = async () => {
      await carregarServidores();
    };
    carregarInicial();
  }, [carregarServidores]);

  return (
    <Layout>
      <Typography variant="h5" fontWeight="bold" sx={{ mb: 3 }}>Servidores</Typography>

      {erro && <Alert severity="error" closeText="Fechar" sx={{ mb: 2 }} onClose={() => setErro('')}>{erro}</Alert>}
      <Alert severity="info" sx={{ mb: 2 }}>
        O cadastro de servidor é realizado durante a abertura de um novo processo.
      </Alert>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>CPF</TableCell>
            <TableCell>PIS/PASEP/NIT</TableCell>
            <TableCell>Sexo</TableCell>
              <TableCell>Matrícula</TableCell>
              <TableCell>Cargo</TableCell>
              <TableCell>Órgão</TableCell>
              <TableCell>Admissão</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={7} align="center"><CircularProgress /></TableCell>
              </TableRow>
            ) : servidores.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center">Nenhum servidor cadastrado</TableCell>
              </TableRow>
            ) : servidores.map((servidor) => (
              <TableRow key={servidor.id} hover>
                <TableCell>{servidor.nome}</TableCell>
                <TableCell>{servidor.cpf}</TableCell>
                <TableCell>{servidor.pis || '-'}</TableCell>
                <TableCell>{servidor.sexo === 'FEMININO' ? 'Feminino' : servidor.sexo === 'MASCULINO' ? 'Masculino' : '-'}</TableCell>
                <TableCell>{servidor.matricula}</TableCell>
                <TableCell>{servidor.cargo}</TableCell>
                <TableCell>{servidor.orgao}</TableCell>
                <TableCell>
                  {servidor.dtAdmissao
                    ? new Date(`${servidor.dtAdmissao}T00:00:00`).toLocaleDateString('pt-BR')
                    : '-'}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Layout>
  );
}
