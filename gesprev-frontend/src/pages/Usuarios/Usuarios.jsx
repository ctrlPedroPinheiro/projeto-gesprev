import { useState, useEffect, useCallback } from 'react';
import Layout from '../../components/Layout/Layout';
import {
  Box, Typography, Button, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow,
  IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Alert, CircularProgress,
  Tooltip, Chip, MenuItem, Select, FormControl,
  InputLabel, Switch, FormControlLabel
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { listarUsuarios, criarUsuario, atualizarUsuario, deletarUsuario } from '../../services/usuarioService';
import { obterMensagemErro } from '../../utils/apiError';

const USUARIO_VAZIO = {
  nome: '', cpf: '', senha: '', perfil: 'ANALISTA', ativo: true
};

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [modalAberto, setModalAberto] = useState(false);
  const [usuarioEditando, setUsuarioEditando] = useState(null);
  const [form, setForm] = useState(USUARIO_VAZIO);
  const [salvando, setSalvando] = useState(false);
  const [dialogExcluir, setDialogExcluir] = useState(null);

  const carregarUsuarios = useCallback(async () => {
    try {
      const data = await listarUsuarios();
      setUsuarios(data);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao carregar usuários'));
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    const carregarInicial = async () => {
      await carregarUsuarios();
    };
    carregarInicial();
  }, [carregarUsuarios]);

  const abrirModal = (usuario = null) => {
    setUsuarioEditando(usuario);
    setForm(usuario ? {
      nome: usuario.nome,
      cpf: usuario.cpf,
      senha: '',
      perfil: usuario.perfil,
      ativo: usuario.ativo
    } : USUARIO_VAZIO);
    setModalAberto(true);
  };

  const fecharModal = () => {
    setModalAberto(false);
    setUsuarioEditando(null);
    setForm(USUARIO_VAZIO);
  };

  const handleSalvar = async () => {
    setSalvando(true);
    try {
      if (usuarioEditando) {
        await atualizarUsuario(usuarioEditando.id, form);
      } else {
        await criarUsuario(form);
      }
      await carregarUsuarios();
      fecharModal();
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao salvar usuário'));
    } finally {
      setSalvando(false);
    }
  };

  const handleExcluir = async () => {
    try {
      await deletarUsuario(dialogExcluir.id);
      await carregarUsuarios();
      setDialogExcluir(null);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao excluir usuário'));
    }
  };

  const formularioValido = Boolean(
    form.nome.trim() &&
    form.cpf.trim() &&
    form.senha.trim() &&
    form.perfil
  );

  return (
    <Layout>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">Usuários</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => abrirModal()}>
          Novo Usuário
        </Button>
      </Box>

      {erro && <Alert severity="error" closeText="Fechar" sx={{ mb: 2 }} onClose={() => setErro('')}>{erro}</Alert>}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>CPF</TableCell>
              <TableCell>Perfil</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Último Login</TableCell>
              <TableCell align="center">Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={6} align="center"><CircularProgress /></TableCell>
              </TableRow>
            ) : usuarios.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">Nenhum usuário cadastrado</TableCell>
              </TableRow>
            ) : usuarios.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.nome}</TableCell>
                <TableCell>{u.cpf}</TableCell>
                <TableCell>
                  <Chip
                    label={u.perfil}
                    color={u.perfil === 'DIRETOR' ? 'primary' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={u.ativo ? 'Ativo' : 'Inativo'}
                    color={u.ativo ? 'success' : 'error'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {u.ultimoLogin ? new Date(u.ultimoLogin).toLocaleString('pt-BR') : 'Nunca'}
                </TableCell>
                <TableCell align="center">
                  <Tooltip title="Editar">
                    <IconButton onClick={() => abrirModal(u)}><EditIcon /></IconButton>
                  </Tooltip>
                  <Tooltip title="Excluir">
                    <IconButton color="error" onClick={() => setDialogExcluir(u)}>
                      <DeleteIcon />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Modal Cadastro/Edição */}
      <Dialog open={modalAberto} onClose={fecharModal} maxWidth="sm" fullWidth>
        <DialogTitle>{usuarioEditando ? 'Editar Usuário' : 'Novo Usuário'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <TextField label="Nome" required fullWidth value={form.nome}
              onChange={(e) => setForm({ ...form, nome: e.target.value })} />
            <TextField label="CPF" required fullWidth value={form.cpf}
              onChange={(e) => setForm({ ...form, cpf: e.target.value })} />
            <TextField
              label={usuarioEditando ? 'Nova Senha' : 'Senha'}
              required
              type="password" fullWidth value={form.senha}
              onChange={(e) => setForm({ ...form, senha: e.target.value })} />
            <FormControl fullWidth>
              <InputLabel>Perfil</InputLabel>
              <Select value={form.perfil} label="Perfil"
                onChange={(e) => setForm({ ...form, perfil: e.target.value })}>
                <MenuItem value="ANALISTA">Analista</MenuItem>
                <MenuItem value="DIRETOR">Diretor</MenuItem>
              </Select>
            </FormControl>
            <FormControlLabel
              control={<Switch checked={form.ativo}
                onChange={(e) => setForm({ ...form, ativo: e.target.checked })} />}
              label="Ativo"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={fecharModal}>Cancelar</Button>
          <Button variant="contained" onClick={handleSalvar} disabled={salvando || !formularioValido}>
            {salvando ? <CircularProgress size={24} /> : 'Salvar'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog Exclusão */}
      <Dialog open={Boolean(dialogExcluir)} onClose={() => setDialogExcluir(null)}>
        <DialogTitle>Confirmar Exclusão</DialogTitle>
        <DialogContent>
          <Typography>Deseja excluir o usuário <strong>{dialogExcluir?.nome}</strong>?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogExcluir(null)}>Cancelar</Button>
          <Button variant="contained" color="error" onClick={handleExcluir}>Excluir</Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}
