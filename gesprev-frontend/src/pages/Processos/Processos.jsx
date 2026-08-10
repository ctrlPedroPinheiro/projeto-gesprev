import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout/Layout';
import {
  Box, Typography, Button, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow,
  IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, Alert, CircularProgress,
  Tooltip, Chip, MenuItem, Select, FormControl, InputLabel
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DeleteIcon from '@mui/icons-material/Delete';
import CancelIcon from '@mui/icons-material/Cancel';
import ReplayIcon from '@mui/icons-material/Replay';
import { listarProcessos, deletarProcesso, rejeitarProcesso, reabrirProcesso, criarProcessoComServidor, preprocessarFichaFuncionalAbertura } from '../../services/processoService';
import { listarServidores } from '../../services/servidorService';
import { atualizarJsonExtraido, uploadDocumento } from '../../services/documentoService';
import { useAuth } from '../../contexts/useAuth';
import FilterListIcon from '@mui/icons-material/FilterList';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import { Grid, Collapse } from '@mui/material';
import { useLocation } from 'react-router-dom';
import { obterMensagemErro } from '../../utils/apiError';


const STATUS_CORES = {
  CADASTRADO: 'default',
  PENDENTE_DOCUMENTO: 'warning',
  EM_ANALISE: 'info',
  EM_CALCULO: 'primary',
  REJEITADO: 'error',
  FINALIZADO: 'success'
};

const STATUS_ROTULOS = {
  CADASTRADO: 'Cadastrado',
  PENDENTE_DOCUMENTO: 'Pendente de documentos',
  EM_ANALISE: 'Em análise',
  EM_CALCULO: 'Em cálculo',
  REJEITADO: 'Rejeitado',
  FINALIZADO: 'Finalizado'
};

const PROCESSO_VAZIO = {
  nome: '', dtNascimento: '', cpf: '', pis: '', sexo: '', email: '',
  matricula: '', cargo: '', orgao: '', dtAdmissao: '',
  numeroProcesso: ''
};

export default function Processos() {
  const location = useLocation();
  const [processos, setProcessos] = useState([]);
  const [servidores, setServidores] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [modalAberto, setModalAberto] = useState(
    () => new URLSearchParams(location.search).get('novo') === 'true'
  );
  const [form, setForm] = useState(PROCESSO_VAZIO);
  const [salvando, setSalvando] = useState(false);
  const [arquivoFichaFuncionalAbertura, setArquivoFichaFuncionalAbertura] = useState(null);
  const [processandoFichaFuncionalAbertura, setProcessandoFichaFuncionalAbertura] = useState(false);
  const [dadosFichaFuncionalExtraidos, setDadosFichaFuncionalExtraidos] = useState(null);
  const [dialogAcao, setDialogAcao] = useState(null);
  const [observacao, setObservacao] = useState('');
  const [dialogExcluir, setDialogExcluir] = useState(null);
  const { usuario } = useAuth();
  const navigate = useNavigate();

  const [filtros, setFiltros] = useState({
    numeroProcesso: '',
    status: '',
    nomeServidor: '',
    cpfServidor: '',
    dtCriacaoInicio: '',
    dtCriacaoFim: ''
  });
  const [filtrosAbertos, setFiltrosAbertos] = useState(false);

  const carregarDados = useCallback(async (filtrosAtivos = {}) => {
    try {
      const params = {};
      if (filtrosAtivos.numeroProcesso) params.numeroProcesso = filtrosAtivos.numeroProcesso;
      if (filtrosAtivos.status) params.status = filtrosAtivos.status;
      if (filtrosAtivos.nomeServidor) params.nomeServidor = filtrosAtivos.nomeServidor;
      if (filtrosAtivos.cpfServidor) params.cpfServidor = filtrosAtivos.cpfServidor;
      if (filtrosAtivos.dtCriacaoInicio) params.dtCriacaoInicio = filtrosAtivos.dtCriacaoInicio + 'T00:00:00';
      if (filtrosAtivos.dtCriacaoFim) params.dtCriacaoFim = filtrosAtivos.dtCriacaoFim + 'T23:59:59';

      const [processosData, servidoresData] = await Promise.all([
        listarProcessos(params),
        listarServidores()
      ]);
      setProcessos(processosData);
      setServidores(servidoresData);
    } catch {
      setErro('Erro ao carregar dados');
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    const carregarInicial = async () => {
      await carregarDados();
    };
    carregarInicial();
  }, [carregarDados]);

  const handleSalvar = async () => {
    setSalvando(true);
    console.log('Form enviado:', form);
    try {
      const processoCriado = await criarProcessoComServidor({
        ...form,
        numeroProcesso: parseInt(form.numeroProcesso)
      });

      if (arquivoFichaFuncionalAbertura) {
        const documentoFichaFuncional = await uploadDocumento(processoCriado.id, arquivoFichaFuncionalAbertura, 'FICHA_FUNCIONAL');
        if (dadosFichaFuncionalExtraidos) {
          const jsonFichaFuncional = {
            matricula: dadosFichaFuncionalExtraidos.matricula ?? null,
            nome: dadosFichaFuncionalExtraidos.nome ?? null,
            dtNascimento: dadosFichaFuncionalExtraidos.dtNascimento ?? null,
            cpf: dadosFichaFuncionalExtraidos.cpf ?? null,
            pis: dadosFichaFuncionalExtraidos.pis ?? null,
            sexo: dadosFichaFuncionalExtraidos.sexo ?? null,
            email: dadosFichaFuncionalExtraidos.email ?? null,
            cargo: dadosFichaFuncionalExtraidos.cargo ?? null,
            orgao: dadosFichaFuncionalExtraidos.orgao ?? null,
            dtAdmissao: dadosFichaFuncionalExtraidos.dtAdmissao ?? null
          };
          await atualizarJsonExtraido(documentoFichaFuncional.id, JSON.stringify(jsonFichaFuncional));
        }
      }

      await carregarDados();
      setModalAberto(false);
      setForm(PROCESSO_VAZIO);
      setArquivoFichaFuncionalAbertura(null);
      setDadosFichaFuncionalExtraidos(null);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao criar processo'));
    } finally {
      setSalvando(false);
    }
  };

  const fecharModalNovoProcesso = () => {
    if (salvando || processandoFichaFuncionalAbertura) return;
    setModalAberto(false);
    setForm(PROCESSO_VAZIO);
    setArquivoFichaFuncionalAbertura(null);
    setDadosFichaFuncionalExtraidos(null);
  };

  const handlePreprocessarFichaFuncional = async () => {
    if (!arquivoFichaFuncionalAbertura) {
      setErro('Selecione uma ficha funcional antes de processar.');
      return;
    }

    setProcessandoFichaFuncionalAbertura(true);
    try {
      const dados = await preprocessarFichaFuncionalAbertura(arquivoFichaFuncionalAbertura);
      setDadosFichaFuncionalExtraidos(dados);
      setForm((atual) => ({
        ...atual,
        nome: dados.nome || atual.nome,
        dtNascimento: dados.dtNascimento || atual.dtNascimento,
        cpf: dados.cpf || atual.cpf,
        pis: dados.pis || atual.pis,
        sexo: dados.sexo && dados.sexo !== 'null' ? dados.sexo : atual.sexo,
        email: dados.email || atual.email,
        matricula: dados.matricula || atual.matricula,
        cargo: dados.cargo || atual.cargo,
        orgao: dados.orgao || atual.orgao,
        dtAdmissao: dados.dtAdmissao || atual.dtAdmissao
      }));
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao processar ficha funcional para abertura do processo'));
    } finally {
      setProcessandoFichaFuncionalAbertura(false);
    }
  };

  const handleAcao = async () => {
    try {
      if (dialogAcao.tipo === 'rejeitar') {
        await rejeitarProcesso(dialogAcao.processo.id, observacao);
      } else {
        await reabrirProcesso(dialogAcao.processo.id, observacao);
      }
      await carregarDados();
      setDialogAcao(null);
      setObservacao('');
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao executar ação'));
    }
  };

  const handleExcluir = async () => {
    try {
      await deletarProcesso(dialogExcluir.id);
      await carregarDados();
      setDialogExcluir(null);
    } catch (err) {
      setErro(obterMensagemErro(err, 'Erro ao excluir processo'));
    }
  };

  const handleBuscar = () => {
    carregarDados(filtros);
  };

  const handleLimpar = () => {
    setFiltros({
      numeroProcesso: '',
      status: '',
      nomeServidor: '',
      cpfServidor: '',
      dtCriacaoInicio: '',
      dtCriacaoFim: ''
    });
    carregarDados();
  };

  const nomeServidor = (id) => servidores.find(s => s.id === id)?.nome || '-';
  const formularioValido = Boolean(
    form.nome.trim() &&
    form.dtNascimento &&
    form.cpf.trim() &&
    form.sexo &&
    form.matricula.trim() &&
    form.cargo.trim() &&
    form.orgao.trim() &&
    form.dtAdmissao &&
    Number(form.numeroProcesso) > 0 &&
    (!form.email || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
  );

  return (
    <Layout>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" fontWeight="bold">Processos de Aposentadoria</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setModalAberto(true)}>
          Novo Processo
        </Button>
      </Box>

      {erro && <Alert severity="error" closeText="Fechar" sx={{ mb: 2 }} onClose={() => setErro('')}>{erro}</Alert>}

      {/* Painel de Filtros */}
      <Paper elevation={1} sx={{ mb: 2 }}>
        <Box
          sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }}
          onClick={() => setFiltrosAbertos(!filtrosAbertos)}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <FilterListIcon />
            <Typography variant="body1" fontWeight="bold">Filtros</Typography>
            {Object.values(filtros).some(v => v !== '') && (
              <Chip label="Filtros ativos" color="primary" size="small" />
            )}
          </Box>
          <Typography variant="body2" color="text.secondary">
            {filtrosAbertos ? 'Ocultar' : 'Expandir'}
          </Typography>
        </Box>

        <Collapse in={filtrosAbertos}>
          <Box sx={{ px: 2, pb: 2 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  label="Nº Processo"
                  fullWidth
                  size="small"
                  value={filtros.numeroProcesso}
                  onChange={(e) => setFiltros({ ...filtros, numeroProcesso: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={filtros.status}
                    label="Status"
                    onChange={(e) => setFiltros({ ...filtros, status: e.target.value })}
                  >
                    <MenuItem value="">Todos</MenuItem>
                    <MenuItem value="CADASTRADO">Cadastrado</MenuItem>
                    <MenuItem value="PENDENTE_DOCUMENTO">Pendente Documento</MenuItem>
                    <MenuItem value="EM_ANALISE">Em Análise</MenuItem>
                    <MenuItem value="EM_CALCULO">Em Cálculo</MenuItem>
                    <MenuItem value="REJEITADO">Rejeitado</MenuItem>
                    <MenuItem value="FINALIZADO">Finalizado</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  label="Nome do Servidor"
                  fullWidth
                  size="small"
                  value={filtros.nomeServidor}
                  onChange={(e) => setFiltros({ ...filtros, nomeServidor: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  label="CPF do Servidor"
                  fullWidth
                  size="small"
                  value={filtros.cpfServidor}
                  onChange={(e) => setFiltros({ ...filtros, cpfServidor: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  label="Data Criação Início"
                  type="date"
                  fullWidth
                  size="small"
                  slotProps={{ inputLabel: { shrink: true } }}
                  value={filtros.dtCriacaoInicio}
                  onChange={(e) => setFiltros({ ...filtros, dtCriacaoInicio: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <TextField
                  label="Data Criação Fim"
                  type="date"
                  fullWidth
                  size="small"
                  slotProps={{ inputLabel: { shrink: true } }}
                  value={filtros.dtCriacaoFim}
                  onChange={(e) => setFiltros({ ...filtros, dtCriacaoFim: e.target.value })}
                />
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    variant="contained"
                    startIcon={<SearchIcon />}
                    onClick={handleBuscar}
                    fullWidth
                  >
                    Buscar
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<ClearIcon />}
                    onClick={handleLimpar}
                    fullWidth
                  >
                    Limpar
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </Collapse>
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nº Processo</TableCell>
              <TableCell>Servidor</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Data Criação</TableCell>
              <TableCell>ltima Atualização</TableCell>
              <TableCell align="center">Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {carregando ? (
              <TableRow>
                <TableCell colSpan={6} align="center"><CircularProgress /></TableCell>
              </TableRow>
            ) : processos.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">Nenhum processo cadastrado</TableCell>
              </TableRow>
            ) : processos.map((p) => (
              <TableRow key={p.id} hover>
                <TableCell>{p.numeroProcesso}</TableCell>
                <TableCell>{nomeServidor(p.servidorId)}</TableCell>
                <TableCell>
                  <Chip
                    label={STATUS_ROTULOS[p.status] || p.status}
                    color={STATUS_CORES[p.status] || 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>{p.dtCriacao ? new Date(p.dtCriacao).toLocaleDateString('pt-BR') : '-'}</TableCell>
                <TableCell>{p.dtAtualizacao ? new Date(p.dtAtualizacao).toLocaleDateString('pt-BR') : '-'}</TableCell>
                <TableCell align="center">
                  <Tooltip title="Ver detalhes">
                    <IconButton onClick={() => navigate(`/processos/${p.id}`)}>
                      <VisibilityIcon />
                    </IconButton>
                  </Tooltip>
                  {p.status === 'EM_CALCULO' && (
                    <Tooltip title="Rejeitar">
                      <IconButton color="error" onClick={() => setDialogAcao({ tipo: 'rejeitar', processo: p })}>
                        <CancelIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                  {p.status === 'REJEITADO' && (
                    <Tooltip title="Reabrir">
                      <IconButton color="primary" onClick={() => setDialogAcao({ tipo: 'reabrir', processo: p })}>
                        <ReplayIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                  {usuario?.perfil === 'DIRETOR' && (
                    <Tooltip title="Excluir">
                      <IconButton color="error" onClick={() => setDialogExcluir(p)}>
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Modal Novo Processo */}
      <Dialog open={modalAberto} onClose={fecharModalNovoProcesso} maxWidth="md" fullWidth>
        <DialogTitle>Novo Processo de Aposentadoria</DialogTitle>
        <DialogContent>
          <Paper variant="outlined" sx={{ p: 2, mt: 1, mb: 3 }}>
            <Typography variant="subtitle1" fontWeight="bold" sx={{ mb: 1 }}>
              Preenchimento por ficha funcional
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Opcional: envie uma ficha funcional para a IA preencher os dados iniciais, incluindo PIS/PASEP/NIT quando constar no documento. Revise tudo antes de criar o processo.
              Ao criar, a ficha funcional selecionada será anexada automaticamente como documento do processo.
            </Typography>

            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, alignItems: 'center' }}>
              <Button variant="outlined" component="label">
                Selecionar ficha funcional
                <input
                  hidden
                  type="file"
                  accept="application/pdf,image/*"
                  onChange={(e) => {
                    const arquivo = e.target.files?.[0] || null;
                    setArquivoFichaFuncionalAbertura(arquivo);
                    setDadosFichaFuncionalExtraidos(null);
                  }}
                />
              </Button>

              <Button
                variant="contained"
                onClick={handlePreprocessarFichaFuncional}
                disabled={!arquivoFichaFuncionalAbertura || processandoFichaFuncionalAbertura}
              >
                {processandoFichaFuncionalAbertura ? <CircularProgress size={22} /> : 'Processar ficha funcional'}
              </Button>

              {arquivoFichaFuncionalAbertura && (
                <Typography variant="body2">
                  Arquivo selecionado: <strong>{arquivoFichaFuncionalAbertura.name}</strong>
                </Typography>
              )}
            </Box>

            {dadosFichaFuncionalExtraidos && (
              <Alert severity="success" sx={{ mt: 2 }}>
                Ficha funcional processada. Os campos encontrados foram preenchidos abaixo, mas ainda podem ser corrigidos manualmente.
              </Alert>
            )}
          </Paper>

          <Typography variant="subtitle1" fontWeight="bold" sx={{ mt: 1, mb: 2 }}>
            Dados do Servidor
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Nome" required fullWidth value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="CPF" required fullWidth value={form.cpf}
                onChange={(e) => setForm({ ...form, cpf: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="PIS/PASEP/NIT" fullWidth value={form.pis}
                onChange={(e) => setForm({ ...form, pis: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControl fullWidth required>
                <InputLabel>Sexo</InputLabel>
                <Select
                  value={form.sexo}
                  label="Sexo"
                  onChange={(e) => setForm({ ...form, sexo: e.target.value })}
                >
                  <MenuItem value="FEMININO">Feminino</MenuItem>
                  <MenuItem value="MASCULINO">Masculino</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Data de Nascimento" required type="date" fullWidth
                slotProps={{ inputLabel: { shrink: true } }} value={form.dtNascimento}
                onChange={(e) => setForm({ ...form, dtNascimento: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Email" fullWidth value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Matrícula" required fullWidth value={form.matricula}
                onChange={(e) => setForm({ ...form, matricula: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Cargo" required fullWidth value={form.cargo}
                onChange={(e) => setForm({ ...form, cargo: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="rgão" required fullWidth value={form.orgao}
                onChange={(e) => setForm({ ...form, orgao: e.target.value })} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Data de Admissão" required type="date" fullWidth
                slotProps={{ inputLabel: { shrink: true } }} value={form.dtAdmissao}
                onChange={(e) => setForm({ ...form, dtAdmissao: e.target.value })} />
            </Grid>
          </Grid>

          <Typography variant="subtitle1" fontWeight="bold" sx={{ mt: 3, mb: 2 }}>
            Dados do Processo
          </Typography>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField label="Número do Processo" required type="number" fullWidth
                slotProps={{ htmlInput: { min: 1 } }}
                value={form.numeroProcesso}
                onChange={(e) => setForm({ ...form, numeroProcesso: e.target.value })} />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={fecharModalNovoProcesso} disabled={salvando || processandoFichaFuncionalAbertura}>Cancelar</Button>
          <Button variant="contained" onClick={handleSalvar} disabled={salvando || !formularioValido}>
            {salvando ? <CircularProgress size={24} /> : 'Criar Processo'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog Rejeitar/Reabrir */}
      <Dialog open={Boolean(dialogAcao)} onClose={() => setDialogAcao(null)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {dialogAcao?.tipo === 'rejeitar' ? 'Rejeitar Processo' : 'Reabrir Processo'}
        </DialogTitle>
        <DialogContent>
          <TextField
            label="Observação"
            multiline
            rows={3}
            fullWidth
            sx={{ mt: 1 }}
            value={observacao}
            onChange={(e) => setObservacao(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogAcao(null)}>Cancelar</Button>
          <Button
            variant="contained"
            color={dialogAcao?.tipo === 'rejeitar' ? 'error' : 'primary'}
            onClick={handleAcao}
            disabled={!observacao.trim()}
          >
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog Excluir */}
      <Dialog open={Boolean(dialogExcluir)} onClose={() => setDialogExcluir(null)}>
        <DialogTitle>Confirmar Exclusão</DialogTitle>
        <DialogContent>
          <Typography>Deseja excluir o processo nº <strong>{dialogExcluir?.numeroProcesso}</strong>?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogExcluir(null)}>Cancelar</Button>
          <Button variant="contained" color="error" onClick={handleExcluir}>Excluir</Button>
        </DialogActions>
      </Dialog>
    </Layout>
  );
}

